package io.github.intisy.ai.ir.stream;

/** Closes the streamed response. Carries no fields beyond the discriminator. */
public final class MessageStopEvent extends IrStreamEvent {
    public MessageStopEvent() {
        super(IrEventType.MESSAGE_STOP);
    }
}
