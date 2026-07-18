package io.github.intisy.ai.ir;

/** A model-issued tool call. {@code input} is a parsed JSON tree (Map/List/String/Number/Boolean/null). */
public final class ToolUseBlock extends Block {
    public String id;
    public String name;
    public Object input;

    public ToolUseBlock() {
        super(BlockKind.TOOL_USE);
    }
}
