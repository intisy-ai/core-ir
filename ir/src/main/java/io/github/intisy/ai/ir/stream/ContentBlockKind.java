package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsStringUnion;

/** {@link ContentBlockStartEvent#blockKind} values. */
@TsStringUnion
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
