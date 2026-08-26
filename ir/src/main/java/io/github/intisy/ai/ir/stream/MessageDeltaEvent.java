package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.ir.IrUsage;

/** Carries the final {@code stopReason} and/or updated usage, ahead of {@link MessageStopEvent}. */
public final class MessageDeltaEvent extends IrStreamEvent {
    /** The response's final stop reason, or null when not yet known. */
    public String stopReason;
    /** Updated token usage, or null when not yet known. */
    public IrUsage usage;

    /** Creates an event with no fields set beyond the discriminator. */
    public MessageDeltaEvent() {
        super(IrEventType.MESSAGE_DELTA);
    }
}
