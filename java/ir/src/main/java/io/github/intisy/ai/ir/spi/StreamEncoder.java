package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.ir.stream.IrStreamEvent;

/**
 * Stateful, per-connection encoder: feed canonical {@link IrStreamEvent}s, get back the vendor's
 * wire text for each one (e.g. one Anthropic SSE {@code event:}/{@code data:} frame).
 */
public interface StreamEncoder {
    String encode(IrStreamEvent event);
}
