package io.github.intisy.ai.js;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.json.IrJson;
import io.github.intisy.ai.ir.spi.HandleIrException;
import io.github.intisy.ai.ir.spi.HandlerCtx;
import io.github.intisy.ai.ir.spi.IrHandler;
import io.github.intisy.ai.ir.spi.IrStreamHandler;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.stream.IrEventSource;
import io.github.intisy.ai.ir.stream.IrStreamEvent;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSPromise;
import org.teavm.jso.core.JSString;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An {@link IrStreamHandler} backed by a JS-provided handler object, so a Java router can call a
 * provider that was loaded by the JS host's dynamic {@code import()}.
 *
 * <p>Both entry points are asynchronous on the JS side and blocking-shaped here, bridged with the
 * same {@code @Async} + {@link AsyncCallback} mechanism as {@code JsHttpClientBridge}. The streamed
 * entry crosses that boundary once PER EVENT, which is supported: the suspension may be repeated
 * inside one Java call.
 */
public final class JsIrHandlerBridge implements IrStreamHandler {

    /** A JS pull source over one streamed response: resolves the next IR event as JSON, or null. */
    public interface JsIrEvents extends JSObject {
        JSPromise<JSString> next();
    }

    /**
     * The JS-provided handler. {@code handleIr} resolves an {@code IrResponse} as JSON;
     * {@code handleIrStream} returns a pull source, and is absent on a buffered-only handler.
     *
     * <p>A rejection carrying a {@code HandleIrError}-shaped JSON payload is reconstructed as a
     * {@link HandleIrException}; anything else becomes a plain failure. The marker is duck-typed
     * because a provider is bundled independently, so class identity never survives the boundary.
     */
    public interface JsIrHandler extends JSObject {
        JSPromise<JSString> handleIr(JSString irRequestJson, JSString ctxJson);

        JsIrEvents handleIrStream(JSString irRequestJson, JSString ctxJson);
    }

    private final String id;
    private final JsIrHandler jsHandler;
    private final JsonCodec json;

    public JsIrHandlerBridge(String id, JsIrHandler jsHandler, JsonCodec json) {
        this.id = id;
        this.jsHandler = jsHandler;
        this.json = json;
    }

    /** Whether the JS handler offers a streamed entry point at all. */
    public boolean canStream() {
        return !JSObjects.isUndefined(streamFn());
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public IrResponse handleIr(IrRequest request, HandlerCtx ctx) throws Exception {
        String responseJson = awaitHandle(jsHandler, JSString.valueOf(IrJson.serializeRequest(json, request)),
                JSString.valueOf(ctxJson(ctx)));
        return IrJson.parseResponse(json, responseJson);
    }

    @Override
    public IrEventSource handleIrStream(IrRequest request, HandlerCtx ctx) {
        final JsIrEvents events = jsHandler.handleIrStream(
                JSString.valueOf(IrJson.serializeRequest(json, request)), JSString.valueOf(ctxJson(ctx)));
        if (events == null || JSObjects.isUndefined(events)) {
            throw new UnsupportedOperationException("the JS handler supplies no streamed entry point");
        }
        return new IrEventSource() {
            private boolean drained;

            @Override
            public IrStreamEvent next() throws Exception {
                if (drained) return null;
                String eventJson = awaitNext(events);
                if (eventJson == null) {
                    drained = true;
                    return null;
                }
                return IrJson.parseStreamEvent(json, eventJson);
            }
        };
    }

    private String ctxJson(HandlerCtx ctx) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("configDir", ctx.configDir);
        out.put("model", ctx.model);
        out.put("handlerId", id);
        return json.stringify(out);
    }

    private JSObject streamFn() {
        return readMember(jsHandler, "handleIrStream");
    }

    @org.teavm.jso.JSBody(params = {"target", "name"}, script = "return target[name];")
    private static native JSObject readMember(JSObject target, String name);

    // -- @Async bridges ------------------------------------------------------------

    @Async
    private static native String awaitHandle(JsIrHandler fn, JSString requestJson, JSString ctxJson)
            throws Exception;

    private static void awaitHandle(JsIrHandler fn, JSString requestJson, JSString ctxJson,
                                    AsyncCallback<String> callback) {
        fn.handleIr(requestJson, ctxJson).then(
                value -> {
                    callback.complete(value == null ? null : value.stringValue());
                    return null;
                },
                error -> {
                    callback.error(toHandlerFailure(error));
                    return null;
                });
    }

    @Async
    private static native String awaitNext(JsIrEvents events) throws Exception;

    private static void awaitNext(JsIrEvents events, AsyncCallback<String> callback) {
        events.next().then(
                value -> {
                    // A drained source resolves null/undefined rather than a JSON event.
                    callback.complete(value == null || JSObjects.isUndefined(value) ? null : value.stringValue());
                    return null;
                },
                error -> {
                    callback.error(toHandlerFailure(error));
                    return null;
                });
    }

    /**
     * Reconstructs a {@link HandleIrException} from a rejection that carries one, so a provider's
     * real status and headers still drive the caller's rate-limit and fallback decisions.
     */
    private static Exception toHandlerFailure(Object error) {
        JSObject rejected = error instanceof JSObject ? (JSObject) error : null;
        if (rejected != null && !JSObjects.isUndefined(readMember(rejected, "status"))) {
            String name = memberString(rejected, "name");
            if (name != null && name.contains("HandleIrError")) {
                return new HandleIrException(memberInt(rejected, "status", 502), headersOf(rejected),
                        memberString(rejected, "body"), retryAfterOf(rejected));
            }
        }
        return new RuntimeException("the JS handler rejected: " + error);
    }

    private static Map<String, String> headersOf(JSObject rejected) {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        JSObject raw = readMember(rejected, "headers");
        if (raw == null || JSObjects.isUndefined(raw)) return headers;
        JSString[] names = ownKeys(raw);
        for (JSString name : names) {
            String key = name.stringValue();
            String value = memberString(raw, key);
            if (value != null) headers.put(key, value);
        }
        return headers;
    }

    private static Long retryAfterOf(JSObject rejected) {
        JSObject raw = readMember(rejected, "retryAfterMs");
        if (raw == null || JSObjects.isUndefined(raw)) return null;
        return (long) memberInt(rejected, "retryAfterMs", 0);
    }

    private static String memberString(JSObject target, String name) {
        JSObject raw = readMember(target, name);
        if (raw == null || JSObjects.isUndefined(raw)) return null;
        return stringify(raw);
    }

    @org.teavm.jso.JSBody(params = {"value"}, script = "return String(value);")
    private static native String stringify(JSObject value);

    @org.teavm.jso.JSBody(params = {"target", "name", "fallback"}, script =
            "var v = target[name]; return typeof v === 'number' ? v : fallback;")
    private static native int memberInt(JSObject target, String name, int fallback);

    @org.teavm.jso.JSBody(params = {"target"}, script = "return Object.keys(target);")
    private static native JSString[] ownKeys(JSObject target);
}
