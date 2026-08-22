package io.github.intisy.ai.ir.stream;

import java.util.Map;

/**
 * Base of the canonical streaming event hierarchy: {@link MessageStartEvent},
 * {@link ContentBlockStartEvent}, {@link TextDeltaEvent}, {@link ThinkingDeltaEvent},
 * {@link ThinkingSignatureEvent}, {@link ToolInputDeltaEvent}, {@link ContentBlockStopEvent},
 * {@link MessageDeltaEvent}, {@link MessageStopEvent}, {@link ErrorEvent}. A vendor's
 * {@code StreamDecoder} maps its SSE chunks to these; its {@code StreamEncoder} maps these back.
 *
 * <p>{@code event} is the JSON discriminator ({@link IrEventType}). {@code extensions} carries
 * vendor-specific passthrough with no neutral home, same role as {@link
 * io.github.intisy.ai.ir.Block#extensions}, so a translator's streaming decode-then-encode round
 * trip stays semantically lossless.
 */
public abstract class IrStreamEvent {
    public String event;
    public Map<String, Object> extensions;

    protected IrStreamEvent(String event) {
        this.event = event;
    }
}
