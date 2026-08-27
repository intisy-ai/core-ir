package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;

/**
 * An in-stream error. Named {@code ErrorEvent} (not {@code Error}) to avoid shadowing
 * {@code java.lang.Error}.
 */
@TsDiscriminant(field = "event", value = IrEventType.ERROR)
@TsInterface(data = true)
public final class ErrorEvent extends IrStreamEvent {
    /** The vendor's own error-type string, or null when the vendor does not report one. */
    @TsOptional
    @TsNullable
    public String errorType;
    /** Human-readable error message. */
    @TsOptional
    @TsNullable
    public String message;

    /** Creates an event with no fields set beyond the discriminator. */
    public ErrorEvent() {
        super(IrEventType.ERROR);
    }
}
