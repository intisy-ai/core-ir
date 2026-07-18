package io.github.intisy.ai.ir;

/** JSON discriminator values for {@link Block} subclasses. */
public final class BlockKind {
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String TOOL_USE = "tool_use";
    public static final String TOOL_RESULT = "tool_result";
    public static final String THINKING = "thinking";
    /** {@link UnknownBlock} -- a block whose wire {@code type} no translator recognizes. */
    public static final String UNKNOWN = "unknown";

    private BlockKind() {
    }
}
