package io.github.intisy.ai.ir.stream;

/** An incremental raw-JSON-text chunk of a tool call's input, for the block at {@code index}. */
public final class ToolInputDeltaEvent extends IrStreamEvent {
    public int index;
    public String partialJson;

    public ToolInputDeltaEvent() {
        super(IrEventType.TOOL_INPUT_DELTA);
    }
}
