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

    /** Parse and stringify with no IR type involved, proving the JSON codec crosses TeaVM. */
    String jsonRoundTrip(String json);

    /** Wire JSON to an IR request and back, proving the request helper crosses TeaVM. */
    String irRequestRoundTrip(String wireJson);

    /** Wire JSON to an IR response and back. */
    String irResponseRoundTrip(String wireJson);

    /** Wire JSON to an IR stream event and back. */
    String irStreamEventRoundTrip(String wireJson);
}
