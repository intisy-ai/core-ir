package io.github.intisy.ai.ir.stream;

/**
 * An in-stream error. Named {@code ErrorEvent} (not {@code Error}) to avoid shadowing
 * {@code java.lang.Error}.
 */
public final class ErrorEvent extends IrStreamEvent {
    public String errorType;
    public String message;

    public ErrorEvent() {
        super(IrEventType.ERROR);
    }
}
