package io.github.intisy.ai.ir;

import java.util.List;

/** The caller's result for a prior {@link ToolUseBlock}, referenced by {@code toolUseId}. */
public final class ToolResultBlock extends Block {
    /** The {@link ToolUseBlock#id} this result answers. */
    public String toolUseId;
    /** The tool's result, as content blocks. */
    public List<Block> content;
    /** Whether the tool call failed, or null when the caller does not report success or failure. */
    public Boolean isError;

    /** Creates an empty tool-result block; the caller sets {@link #toolUseId} and {@link #content}. */
    public ToolResultBlock() {
        super(BlockKind.TOOL_RESULT);
    }
}
