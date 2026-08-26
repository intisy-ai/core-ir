package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.ir.IrUsage;

/** Opens a streamed response: the message id/model/role and (if known up front) partial usage. */
public final class MessageStartEvent extends IrStreamEvent {
    /** The response message's id. */
    public String id;
    /** The model that produced the response. */
    public String model;
    /** The response message's role, typically {@code assistant}. */
    public String role;
    /** Token usage known at stream start, or null when the vendor reports it only later. */
    public IrUsage usage;

    /** Creates an event with no fields set beyond the discriminator. */
    public MessageStartEvent() {
        super(IrEventType.MESSAGE_START);
    }
}
