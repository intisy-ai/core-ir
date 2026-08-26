package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.ir.stream.IrStreamEvent;

/**
 * Stateful, per-connection encoder: feed canonical {@link IrStreamEvent}s, get back the vendor's
 * wire text for each one (e.g. one SSE {@code event:}/{@code data:} frame).
 */
public interface StreamEncoder {
    /**
     * @param event the canonical event to encode.
     * @return the vendor's wire text for this event, or null when the vendor has no frame for it.
     */
    String encode(IrStreamEvent event);
}
