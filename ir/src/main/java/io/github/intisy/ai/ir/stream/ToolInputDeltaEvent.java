package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;

/** An incremental raw-JSON-text chunk of a tool call's input, for the block at {@code index}. */
@TsDiscriminant(field = "event", value = IrEventType.TOOL_INPUT_DELTA)
@TsInterface(data = true)
public final class ToolInputDeltaEvent extends IrStreamEvent {
    /** Position of the content block this chunk belongs to. */
    public int index;
    /** The incremental raw-JSON-text chunk of the tool call's input. */
    @TsOptional
    @TsNullable
    public String partialJson;

    /** Creates an event with no fields set beyond the discriminator. */
    public ToolInputDeltaEvent() {
        super(IrEventType.TOOL_INPUT_DELTA);
    }
}
