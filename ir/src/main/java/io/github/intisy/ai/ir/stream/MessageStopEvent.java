package io.github.intisy.ai.ir.stream;

/** Closes the streamed response. Carries no fields beyond the discriminator. */
public final class MessageStopEvent extends IrStreamEvent {
    /** Creates an event with no fields set beyond the discriminator. */
    public MessageStopEvent() {
        super(IrEventType.MESSAGE_STOP);
    }
}
