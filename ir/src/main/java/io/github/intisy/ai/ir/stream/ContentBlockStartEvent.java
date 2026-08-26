package io.github.intisy.ai.ir.stream;

/**
 * Opens a content block at {@code index}. {@code blockKind} is one of {@link ContentBlockKind};
 * {@code toolUseId}/{@code toolName} are set only when {@code blockKind} is
 * {@link ContentBlockKind#TOOL_USE}.
 */
public final class ContentBlockStartEvent extends IrStreamEvent {
    /** Position of the content block within the message. */
    public int index;
    /** One of the {@link ContentBlockKind} constants. */
    public String blockKind;
    /** The tool call's id, set only when {@link #blockKind} is {@link ContentBlockKind#TOOL_USE}. */
    public String toolUseId;
    /** The tool's name, set only when {@link #blockKind} is {@link ContentBlockKind#TOOL_USE}. */
    public String toolName;

    /** Creates an event with no fields set beyond the discriminator. */
    public ContentBlockStartEvent() {
        super(IrEventType.CONTENT_BLOCK_START);
    }
}
