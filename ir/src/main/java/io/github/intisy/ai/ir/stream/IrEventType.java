package io.github.intisy.ai.ir.stream;

/** JSON discriminator values for {@link IrStreamEvent} subclasses. */
public final class IrEventType {
    /** Discriminator for {@link MessageStartEvent}. */
    public static final String MESSAGE_START = "message_start";
    /** Discriminator for {@link ContentBlockStartEvent}. */
    public static final String CONTENT_BLOCK_START = "content_block_start";
    /** Discriminator for {@link TextDeltaEvent}. */
    public static final String TEXT_DELTA = "text_delta";
    /** Discriminator for {@link ThinkingDeltaEvent}. */
    public static final String THINKING_DELTA = "thinking_delta";
    /** Discriminator for {@link ThinkingSignatureEvent}. */
    public static final String THINKING_SIGNATURE = "thinking_signature";
    /** Discriminator for {@link ToolInputDeltaEvent}. */
    public static final String TOOL_INPUT_DELTA = "tool_input_delta";
    /** Discriminator for {@link ContentBlockStopEvent}. */
    public static final String CONTENT_BLOCK_STOP = "content_block_stop";
    /** Discriminator for {@link MessageDeltaEvent}. */
    public static final String MESSAGE_DELTA = "message_delta";
    /** Discriminator for {@link MessageStopEvent}. */
    public static final String MESSAGE_STOP = "message_stop";
    /** Discriminator for {@link ErrorEvent}. */
    public static final String ERROR = "error";

    private IrEventType() {
    }
}
