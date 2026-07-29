package io.github.intisy.ai.js;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.json.IrJson;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.stream.IrStreamEvent;

import org.teavm.jso.JSExport;

/**
 * TeaVM JS export surface over core-ir's neutral IR types: bare round-trip smoke exports proving
 * the pipeline is wired end to end. Mirrors core-proxy's {@code io.github.intisy.ai.js.CoreProxyJs}
 * export style (bare static {@code @JSExport} methods over JSON strings, no gson, no reflection).
 * Vendor translators live in their own {@code *-translator} repos, each with its own TeaVM export
 * surface.
 */
public final class CoreIrJs {
    private CoreIrJs() {
    }

    /**
     * Bare parse+stringify round trip through {@link SimpleJsonCodec}, with no IR type involved --
     * proves the JSON codec itself is wired through TeaVM correctly.
     */
    @JSExport
    public static String jsonRoundTrip(String json) {
        JsonCodec codec = new SimpleJsonCodec();
        return codec.stringify(codec.parse(json));
    }

    /** {@code wireJson -> IrRequest -> wireJson}, proving the IR request (de)serialize helper through TeaVM. */
    @JSExport
    public static String irRequestRoundTrip(String wireJson) {
        JsonCodec json = new SimpleJsonCodec();
        IrRequest request = IrJson.parseRequest(json, wireJson);
        return IrJson.serializeRequest(json, request);
    }

    /** {@code wireJson -> IrResponse -> wireJson}. */
    @JSExport
    public static String irResponseRoundTrip(String wireJson) {
        JsonCodec json = new SimpleJsonCodec();
        IrResponse response = IrJson.parseResponse(json, wireJson);
        return IrJson.serializeResponse(json, response);
    }

    /** {@code wireJson -> IrStreamEvent -> wireJson}. */
    @JSExport
    public static String irStreamEventRoundTrip(String wireJson) {
        JsonCodec json = new SimpleJsonCodec();
        IrStreamEvent event = IrJson.parseStreamEvent(json, wireJson);
        return IrJson.serializeStreamEvent(json, event);
    }
}
