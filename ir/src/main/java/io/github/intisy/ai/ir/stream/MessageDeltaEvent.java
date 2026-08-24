package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.ir.IrUsage;

/** Carries the final {@code stopReason} and/or updated usage, ahead of {@link MessageStopEvent}. */
public final class MessageDeltaEvent extends IrStreamEvent {
    public String stopReason;
    public IrUsage usage;

    public MessageDeltaEvent() {
        super(IrEventType.MESSAGE_DELTA);
    }
}
