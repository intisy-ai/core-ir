package io.github.intisy.ai.ir;

/** A model-issued tool call. {@code input} is a parsed JSON tree (Map/List/String/Number/Boolean/null). */
public final class ToolUseBlock extends Block {
    /** An identifier for this call, referenced by the matching {@link ToolResultBlock#toolUseId}. */
    public String id;
    /** The {@link IrTool#name} being called. */
    public String name;
    /** The call's arguments, as a parsed JSON tree. */
    public Object input;

    /** Creates an empty tool-use block; the caller sets {@link #id}, {@link #name} and {@link #input}. */
    public ToolUseBlock() {
        super(BlockKind.TOOL_USE);
    }
}
