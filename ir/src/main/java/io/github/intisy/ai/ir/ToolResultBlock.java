package io.github.intisy.ai.ir;

import java.util.List;

/** The caller's result for a prior {@link ToolUseBlock}, referenced by {@code toolUseId}. */
public final class ToolResultBlock extends Block {
    public String toolUseId;
    public List<Block> content;
    public Boolean isError;

    public ToolResultBlock() {
        super(BlockKind.TOOL_RESULT);
    }
}
