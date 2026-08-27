package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** The caller's result for a prior {@link ToolUseBlock}, referenced by {@code toolUseId}. */
@TsDiscriminant(field = "kind", value = BlockKind.TOOL_RESULT)
@TsInterface(data = true)
public final class ToolResultBlock extends Block {
    /** The {@link ToolUseBlock#id} this result answers. */
    public String toolUseId;
    /** The tool's result, as content blocks. */
    public List<Block> content;
    /** Whether the tool call failed, or null when the caller does not report success or failure. */
    @TsOptional
    @TsNullable
    public Boolean isError;

    /** Creates an empty tool-result block; the caller sets {@link #toolUseId} and {@link #content}. */
    public ToolResultBlock() {
        super(BlockKind.TOOL_RESULT);
    }
}
