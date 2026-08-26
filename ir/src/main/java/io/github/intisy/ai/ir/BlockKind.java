package io.github.intisy.ai.ir;

/** JSON discriminator values for {@link Block} subclasses. */
public final class BlockKind {
    /** {@link TextBlock}'s discriminator. */
    public static final String TEXT = "text";
    /** {@link ImageBlock}'s discriminator. */
    public static final String IMAGE = "image";
    /** {@link ToolUseBlock}'s discriminator. */
    public static final String TOOL_USE = "tool_use";
    /** {@link ToolResultBlock}'s discriminator. */
    public static final String TOOL_RESULT = "tool_result";
    /** {@link ThinkingBlock}'s discriminator. */
    public static final String THINKING = "thinking";
    /** {@link UnknownBlock} -- a block whose wire {@code type} no translator recognizes. */
    public static final String UNKNOWN = "unknown";

    private BlockKind() {
    }
}
