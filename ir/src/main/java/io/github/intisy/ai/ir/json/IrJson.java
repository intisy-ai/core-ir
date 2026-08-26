package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.stream.IrStreamEvent;

import java.util.Map;

/**
 * Public (de)serialize surface for the IR types, over an injected {@link JsonCodec} (no gson, no
 * reflection -- same SPI-injection pattern as core-proxy's {@code Router}). Every IR type hand-
 * rolls the conversion between {@code Map<String,Object>} and POJOs (see the package-private
 * {@code *Json} helpers), so this stays TeaVM-transpilable.
 */
public final class IrJson {
    private IrJson() {
    }

    /**
     * @param json the codec to serialize with.
     * @param request the request to serialize.
     * @return the request as JSON text.
     */
    public static String serializeRequest(JsonCodec json, IrRequest request) {
        return json.stringify(IrRequestJson.toMap(request));
    }

    /**
     * @param json the codec to parse with.
     * @param text the request's JSON text.
     * @return the parsed request.
     */
    public static IrRequest parseRequest(JsonCodec json, String text) {
        Object parsed = json.parse(text);
        return IrRequestJson.fromMap(JsonUtil.asMap(parsed));
    }

    /**
     * @param json the codec to serialize with.
     * @param response the response to serialize.
     * @return the response as JSON text.
     */
    public static String serializeResponse(JsonCodec json, IrResponse response) {
        return json.stringify(IrResponseJson.toMap(response));
    }

    /**
     * @param json the codec to parse with.
     * @param text the response's JSON text.
     * @return the parsed response.
     */
    public static IrResponse parseResponse(JsonCodec json, String text) {
        Object parsed = json.parse(text);
        return IrResponseJson.fromMap(JsonUtil.asMap(parsed));
    }

    /**
     * @param json the codec to serialize with.
     * @param event the stream event to serialize.
     * @return the event as JSON text.
     */
    public static String serializeStreamEvent(JsonCodec json, IrStreamEvent event) {
        return json.stringify(IrStreamEventJson.toMap(event));
    }

    /**
     * @param json the codec to parse with.
     * @param text the stream event's JSON text.
     * @return the parsed stream event.
     */
    public static IrStreamEvent parseStreamEvent(JsonCodec json, String text) {
        Object parsed = json.parse(text);
        return IrStreamEventJson.fromMap(JsonUtil.asMap(parsed));
    }

    /**
     * Escape hatch for callers that already hold a parsed {@code Map<String,Object>} tree.
     *
     * @param request the request to convert.
     * @return the request as a {@code Map<String,Object>} tree.
     */
    public static Map<String, Object> toMap(IrRequest request) {
        return IrRequestJson.toMap(request);
    }

    /**
     * @param response the response to convert.
     * @return the response as a {@code Map<String,Object>} tree.
     */
    public static Map<String, Object> toMap(IrResponse response) {
        return IrResponseJson.toMap(response);
    }

    /**
     * @param event the stream event to convert.
     * @return the event as a {@code Map<String,Object>} tree.
     */
    public static Map<String, Object> toMap(IrStreamEvent event) {
        return IrStreamEventJson.toMap(event);
    }
}
