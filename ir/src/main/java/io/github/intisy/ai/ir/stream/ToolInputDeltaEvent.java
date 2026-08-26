package io.github.intisy.ai.ir.stream;

/** An incremental raw-JSON-text chunk of a tool call's input, for the block at {@code index}. */
public final class ToolInputDeltaEvent extends IrStreamEvent {
    /** Position of the content block this chunk belongs to. */
    public int index;
    /** The incremental raw-JSON-text chunk of the tool call's input. */
    public String partialJson;

    /** Creates an event with no fields set beyond the discriminator. */
    public ToolInputDeltaEvent() {
        super(IrEventType.TOOL_INPUT_DELTA);
    }
}
