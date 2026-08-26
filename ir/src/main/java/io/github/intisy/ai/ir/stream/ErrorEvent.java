package io.github.intisy.ai.ir.stream;

/**
 * An in-stream error. Named {@code ErrorEvent} (not {@code Error}) to avoid shadowing
 * {@code java.lang.Error}.
 */
public final class ErrorEvent extends IrStreamEvent {
    /** The vendor's own error-type string, or null when the vendor does not report one. */
    public String errorType;
    /** Human-readable error message. */
    public String message;

    /** Creates an event with no fields set beyond the discriminator. */
    public ErrorEvent() {
        super(IrEventType.ERROR);
    }
}
