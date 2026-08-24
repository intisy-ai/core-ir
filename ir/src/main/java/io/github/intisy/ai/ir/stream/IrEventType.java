package io.github.intisy.ai.ir.stream;

/** JSON discriminator values for {@link IrStreamEvent} subclasses. */
public final class IrEventType {
    public static final String MESSAGE_START = "message_start";
    public static final String CONTENT_BLOCK_START = "content_block_start";
    public static final String TEXT_DELTA = "text_delta";
    public static final String THINKING_DELTA = "thinking_delta";
    public static final String THINKING_SIGNATURE = "thinking_signature";
    public static final String TOOL_INPUT_DELTA = "tool_input_delta";
    public static final String CONTENT_BLOCK_STOP = "content_block_stop";
    public static final String MESSAGE_DELTA = "message_delta";
    public static final String MESSAGE_STOP = "message_stop";
    public static final String ERROR = "error";

    private IrEventType() {
    }
}
