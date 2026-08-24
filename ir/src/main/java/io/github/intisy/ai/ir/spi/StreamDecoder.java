package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.ir.stream.IrStreamEvent;

import java.util.List;

/**
 * Stateful, per-connection decoder: feed vendor stream chunks (SSE text, JSON lines, ...) as
 * they arrive, get back the canonical {@link IrStreamEvent}s completed by this chunk. A single
 * chunk may complete zero, one, or several vendor frames; a vendor frame may itself complete
 * zero (e.g. a transport keepalive) or one IR event -- so {@link #decode} returns a list, not a
 * single event.
 */
public interface StreamDecoder {
    List<IrStreamEvent> decode(String chunk);
}
