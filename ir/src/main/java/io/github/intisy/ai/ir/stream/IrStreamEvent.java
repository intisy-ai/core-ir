package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;
import io.github.intisy.ai.tsemit.TsUnionType;
import java.util.Map;

/**
 * Base of the canonical streaming event hierarchy: {@link MessageStartEvent},
 * {@link ContentBlockStartEvent}, {@link TextDeltaEvent}, {@link ThinkingDeltaEvent},
 * {@link ThinkingSignatureEvent}, {@link ToolInputDeltaEvent}, {@link ContentBlockStopEvent},
 * {@link MessageDeltaEvent}, {@link MessageStopEvent}, {@link ErrorEvent}. A vendor's
 * {@code StreamDecoder} maps its SSE chunks to these; its {@code StreamEncoder} maps these back.
 *
 * <p>{@code event} is the JSON discriminator ({@link IrEventType}). {@code extensions} carries
 * vendor-specific passthrough with no neutral home, the same role as
 * {@link io.github.intisy.ai.ir.Block#extensions}, so a translator's streaming decode-then-encode round
 * trip stays semantically lossless.
 */
@TsUnionType
public abstract class IrStreamEvent {
    /** The JSON discriminator, one of the {@link IrEventType} constants. */
    public String event;
    /** Vendor-specific passthrough with no neutral home, or null when none applies. */
    @TsOptional
    @TsNullable
    public Map<String, Object> extensions;

    /**
     * @param event the JSON discriminator, one of the {@link IrEventType} constants.
     */
    protected IrStreamEvent(String event) {
        this.event = event;
    }
}
