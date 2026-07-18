package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.ir.IrUsage;

/** Opens a streamed response: the message id/model/role and (if known up front) partial usage. */
public final class MessageStartEvent extends IrStreamEvent {
    public String id;
    public String model;
    public String role;
    public IrUsage usage;

    public MessageStartEvent() {
        super(IrEventType.MESSAGE_START);
    }
}
