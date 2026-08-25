package io.github.intisy.ai.js;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.json.IrJson;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.spi.StreamDecoder;
import io.github.intisy.ai.ir.spi.StreamEncoder;
import io.github.intisy.ai.ir.spi.Translator;
import io.github.intisy.ai.ir.stream.IrStreamEvent;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Translator} that delegates to a JS-provided translator handle, so a bundle can speak a
 * vendor's wire format without embedding that vendor's translator.
 *
 * @implNote Embedding would be the obvious alternative and is what the Java-plugin-weight rule
 * forbids: a TeaVM bundle statically links everything it reaches, so three vendors linked into every
 * bundle that routes is three private copies. The handle this bridges to is itself transpiled Java,
 * shared once per home, and every one of its entry points is already string-in/string-out, which is
 * why this bridge is pure marshalling and holds no vendor knowledge.
 *
 * <p>Every call here is synchronous. The JS handle must therefore expose the transpiled translator's
 * own string functions, not the promise-returning {@code VendorTranslator} wrapper built on top of
 * them.
 */
public final class JsTranslatorBridge implements Translator {

    /** A JS stateful stream handle: {@code { encode(irEventJson) }} or {@code { decode(chunk) }}. */
    public interface JsStreamHandle extends JSObject {
        JSString encode(JSString irEventJson);

        JSString decode(JSString chunk);
    }

    /**
     * The JS-provided translator handle. Each member is the transpiled translator's own function,
     * taking and returning JSON strings.
     *
     * <p>The two {@code newStream*} members are functions returning a fresh {@link JsStreamHandle},
     * because a stream handle is stateful per connection.
     */
    public interface JsTranslator extends JSObject {
        JSString decodeRequest(JSString wireJson);

        JSString encodeRequest(JSString irRequestJson);

        JSString decodeResponse(JSString wireJson);

        JSString encodeResponse(JSString irResponseJson);

        JsStreamHandle newStreamDecoder();

        JsStreamHandle newStreamEncoder();
    }

    private final JsTranslator jsTranslator;
    private final JsonCodec json;

    public JsTranslatorBridge(JsTranslator jsTranslator, JsonCodec json) {
        this.jsTranslator = jsTranslator;
        this.json = json;
    }

    @Override
    public IrRequest decodeRequest(String wireJson) {
        return IrJson.parseRequest(json, required(jsTranslator.decodeRequest(JSString.valueOf(wireJson)), "decodeRequest"));
    }

    @Override
    public String encodeRequest(IrRequest request) {
        String irJson = IrJson.serializeRequest(json, request);
        return required(jsTranslator.encodeRequest(JSString.valueOf(irJson)), "encodeRequest");
    }

    @Override
    public IrResponse decodeResponse(String wireJson) {
        return IrJson.parseResponse(json, required(jsTranslator.decodeResponse(JSString.valueOf(wireJson)), "decodeResponse"));
    }

    @Override
    public String encodeResponse(IrResponse response) {
        String irJson = IrJson.serializeResponse(json, response);
        return required(jsTranslator.encodeResponse(JSString.valueOf(irJson)), "encodeResponse");
    }

    @Override
    public StreamDecoder newStreamDecoder() {
        final JsStreamHandle handle = jsTranslator.newStreamDecoder();
        if (handle == null || JSObjects.isUndefined(handle)) {
            throw new UnsupportedOperationException("the JS translator handle supplies no stream decoder");
        }
        return new StreamDecoder() {
            @Override
            public List<IrStreamEvent> decode(String chunk) {
                String eventsJson = required(handle.decode(JSString.valueOf(chunk)), "decode");
                List<IrStreamEvent> events = new ArrayList<IrStreamEvent>();
                Object parsed = json.parse(eventsJson);
                if (parsed instanceof List) {
                    for (Object entry : (List<?>) parsed) {
                        events.add(IrJson.parseStreamEvent(json, json.stringify(entry)));
                    }
                }
                return events;
            }
        };
    }

    @Override
    public StreamEncoder newStreamEncoder() {
        final JsStreamHandle handle = jsTranslator.newStreamEncoder();
        if (handle == null || JSObjects.isUndefined(handle)) {
            throw new UnsupportedOperationException("the JS translator handle supplies no stream encoder");
        }
        return new StreamEncoder() {
            @Override
            public String encode(IrStreamEvent event) {
                String eventJson = IrJson.serializeStreamEvent(json, event);
                JSString wire = handle.encode(JSString.valueOf(eventJson));
                // An encoder legitimately returns nothing for an event its vendor has no frame for,
                // which the TS wrapper drops rather than enqueues.
                return wire == null || JSObjects.isUndefined(wire) ? null : wire.stringValue();
            }
        };
    }

    private static String required(JSString value, String what) {
        if (value == null || JSObjects.isUndefined(value)) {
            throw new IllegalStateException("the JS translator returned nothing from " + what);
        }
        return value.stringValue();
    }
}
