package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.stream.IrStreamEvent;

import java.util.Map;

/**
 * Public (de)serialize surface for the IR types, over an injected {@link JsonCodec} (no gson, no
 * reflection -- same SPI-injection pattern as core-proxy's {@code Router}). Every IR type hand-
 * rolls its {@code Map<String,Object>} <-> POJO conversion (see the package-private
 * {@code *Json} helpers), so this stays TeaVM-transpilable.
 */
public final class IrJson {
    private IrJson() {
    }

    public static String serializeRequest(JsonCodec json, IrRequest request) {
        return json.stringify(IrRequestJson.toMap(request));
    }

    public static IrRequest parseRequest(JsonCodec json, String text) {
        Object parsed = json.parse(text);
        return IrRequestJson.fromMap(JsonUtil.asMap(parsed));
    }

    public static String serializeResponse(JsonCodec json, IrResponse response) {
        return json.stringify(IrResponseJson.toMap(response));
    }

    public static IrResponse parseResponse(JsonCodec json, String text) {
        Object parsed = json.parse(text);
        return IrResponseJson.fromMap(JsonUtil.asMap(parsed));
    }

    public static String serializeStreamEvent(JsonCodec json, IrStreamEvent event) {
        return json.stringify(IrStreamEventJson.toMap(event));
    }

    public static IrStreamEvent parseStreamEvent(JsonCodec json, String text) {
        Object parsed = json.parse(text);
        return IrStreamEventJson.fromMap(JsonUtil.asMap(parsed));
    }

    /** Escape hatch for callers that already hold a parsed {@code Map<String,Object>} tree. */
    public static Map<String, Object> toMap(IrRequest request) {
        return IrRequestJson.toMap(request);
    }

    public static Map<String, Object> toMap(IrResponse response) {
        return IrResponseJson.toMap(response);
    }

    public static Map<String, Object> toMap(IrStreamEvent event) {
        return IrStreamEventJson.toMap(event);
    }
}
