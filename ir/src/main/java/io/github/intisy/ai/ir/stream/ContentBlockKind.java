package io.github.intisy.ai.ir.stream;

/** {@link ContentBlockStartEvent#blockKind} values. */
public final class ContentBlockKind {
    public static final String TEXT = "text";
    public static final String TOOL_USE = "tool_use";
    public static final String THINKING = "thinking";

    private ContentBlockKind() {
    }
}
