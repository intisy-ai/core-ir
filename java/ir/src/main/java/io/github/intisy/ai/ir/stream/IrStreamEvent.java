package io.github.intisy.ai.ir.stream;

/**
 * Base of the canonical streaming event hierarchy (see the canonical IR design doc's "Streaming
 * event model" section): {@link MessageStartEvent}, {@link ContentBlockStartEvent},
 * {@link TextDeltaEvent}, {@link ThinkingDeltaEvent}, {@link ThinkingSignatureEvent},
 * {@link ToolInputDeltaEvent}, {@link ContentBlockStopEvent}, {@link MessageDeltaEvent},
 * {@link MessageStopEvent}, {@link ErrorEvent}. A vendor's {@code StreamDecoder} maps its SSE
 * chunks to these; its {@code StreamEncoder} maps these back.
 *
 * <p>{@code event} is the JSON discriminator ({@link IrEventType}).
 */
public abstract class IrStreamEvent {
    public String event;

    protected IrStreamEvent(String event) {
        this.event = event;
    }
}
