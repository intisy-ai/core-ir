package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsModule;

/**
 * The JavaScript module surface {@link io.github.intisy.ai.js.CoreIrJs} exports, typed for a
 * TypeScript consumer.
 *
 * @implNote Never implemented, only emitted: {@link TsModule} renders its members as free functions,
 * which is the shape a TeaVM ES2015 module actually exports. Every member takes and returns wire
 * JSON, because these are round-trip probes over the neutral IR rather than an API a caller reads
 * field by field.
 */
@TsModule
public interface CoreIrSurface {

    /**
     * Parse and stringify with no IR type involved, proving the JSON codec crosses TeaVM.
     *
     * @param json the JSON text to round-trip.
     * @return the same value, re-serialized.
     */
    String jsonRoundTrip(String json);

    /**
     * Wire JSON to an IR request and back, proving the request helper crosses TeaVM.
     *
     * @param wireJson the request's JSON text.
     * @return the same request, re-serialized.
     */
    String irRequestRoundTrip(String wireJson);

    /**
     * Wire JSON to an IR response and back.
     *
     * @param wireJson the response's JSON text.
     * @return the same response, re-serialized.
     */
    String irResponseRoundTrip(String wireJson);

    /**
     * Wire JSON to an IR stream event and back.
     *
     * @param wireJson the stream event's JSON text.
     * @return the same event, re-serialized.
     */
    String irStreamEventRoundTrip(String wireJson);
}
