package io.github.intisy.ai.ir.stream;

/**
 * Opens a content block at {@code index}. {@code blockKind} is one of {@link ContentBlockKind};
 * {@code toolUseId}/{@code toolName} are set only when {@code blockKind} is
 * {@link ContentBlockKind#TOOL_USE}.
 */
public final class ContentBlockStartEvent extends IrStreamEvent {
    public int index;
    public String blockKind;
    public String toolUseId;
    public String toolName;

    public ContentBlockStartEvent() {
        super(IrEventType.CONTENT_BLOCK_START);
    }
}
