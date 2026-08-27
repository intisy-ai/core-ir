package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.ir.IrUsage;
import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;

/** Opens a streamed response: the message id/model/role and (if known up front) partial usage. */
@TsDiscriminant(field = "event", value = IrEventType.MESSAGE_START)
@TsInterface(data = true)
public final class MessageStartEvent extends IrStreamEvent {
    /** The response message's id. */
    @TsOptional
    @TsNullable
    public String id;
    /** The model that produced the response. */
    @TsOptional
    @TsNullable
    public String model;
    /** The response message's role, typically {@code assistant}. */
    @TsOptional
    @TsNullable
    public String role;
    /** Token usage known at stream start, or null when the vendor reports it only later. */
    @TsOptional
    @TsNullable
    public IrUsage usage;

    /** Creates an event with no fields set beyond the discriminator. */
    public MessageStartEvent() {
        super(IrEventType.MESSAGE_START);
    }
}
