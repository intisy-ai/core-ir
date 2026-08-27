package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;

/** Closes the streamed response. Carries no fields beyond the discriminator. */
@TsDiscriminant(field = "event", value = IrEventType.MESSAGE_STOP)
@TsInterface(data = true)
public final class MessageStopEvent extends IrStreamEvent {
    /** Creates an event with no fields set beyond the discriminator. */
    public MessageStopEvent() {
        super(IrEventType.MESSAGE_STOP);
    }
}
