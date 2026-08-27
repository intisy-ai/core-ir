package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;
import io.github.intisy.ai.tsemit.TsVocabulary;

/**
 * Opens a content block at {@code index}. {@code blockKind} is one of {@link ContentBlockKind};
 * {@code toolUseId}/{@code toolName} are set only when {@code blockKind} is
 * {@code tool_use}.
 */
@TsDiscriminant(field = "event", value = IrEventType.CONTENT_BLOCK_START)
@TsInterface(data = true)
public final class ContentBlockStartEvent extends IrStreamEvent {
    /** Position of the content block within the message. */
    public int index;
    /** One of the {@link ContentBlockKind} constants. */
    @TsVocabulary(ContentBlockKind.class)
    public String blockKind;
    /** The tool call's id, set only when {@link #blockKind} is {@code tool_use}. */
    @TsOptional
    @TsNullable
    public String toolUseId;
    /** The tool's name, set only when {@link #blockKind} is {@code tool_use}. */
    @TsOptional
    @TsNullable
    public String toolName;

    /** Creates an event with no fields set beyond the discriminator. */
    public ContentBlockStartEvent() {
        super(IrEventType.CONTENT_BLOCK_START);
    }
}
