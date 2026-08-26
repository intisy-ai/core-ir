package io.github.intisy.ai.ir.stream;

/** {@link ContentBlockStartEvent#blockKind} values. */
public final class ContentBlockKind {
    /** A plain-text content block. */
    public static final String TEXT = "text";
    /** A tool-call content block. */
    public static final String TOOL_USE = "tool_use";
    /** An extended-thinking content block. */
    public static final String THINKING = "thinking";

    private ContentBlockKind() {
    }
}
