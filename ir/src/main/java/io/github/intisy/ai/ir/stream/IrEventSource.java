package io.github.intisy.ai.ir.stream;

/**
 * Blocking-shaped pull source of canonical stream events; implementations handle any async plumbing
 * internally.
 *
 * @implNote Typed on {@link IrStreamEvent} rather than on strings, because this layer owns the IR
 * vocabulary and a caller that had to serialise each event only to re-parse it would pay for a
 * round trip the type system can avoid. The string-shaped transport seam lives a layer below, and a
 * translator's {@code StreamEncoder} is what maps between them.
 */
public interface IrEventSource {

    /**
     * The next event, or {@code null} once the stream is complete.
     *
     * @return the next stream event, or null once the stream is complete.
     * @throws Exception when the underlying stream fails. A failure is terminal: the source must not
     * be pulled again afterwards. Declared broadly on purpose, so a modeled transport outcome such as
     * {@code HandleIrException} can surface from the FIRST pull, which is the only point at which a
     * caller walking a fallback chain can still act on it.
     */
    IrStreamEvent next() throws Exception;
}
