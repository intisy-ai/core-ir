package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsOpen;
import io.github.intisy.ai.tsemit.TsStringUnion;

/**
 * {@link IrResponse#stopReason} / streaming {@code MessageDelta.stopReason} constants.
 *
 * @implNote Open, because a vendor may report a reason this set has no reading of yet and a
 * translator carries it through rather than losing it.
 */
@TsOpen
@TsStringUnion
public final class IrStopReason {
    /** Generation ended normally. */
    public static final String END_TURN = "end_turn";
    /** Generation stopped because {@link IrRequest#maxTokens} was reached. */
    public static final String MAX_TOKENS = "max_tokens";
    /** Generation stopped so the caller can run a tool the model requested. */
    public static final String TOOL_USE = "tool_use";
    /** Generation stopped because a string in {@link IrRequest#stopSequences} was produced. */
    public static final String STOP_SEQUENCE = "stop_sequence";
    /** Generation was paused mid-turn by the vendor. */
    public static final String PAUSE_TURN = "pause_turn";
    /** The model declined to continue generating. */
    public static final String REFUSAL = "refusal";
    /** Generation stopped because of an error. */
    public static final String ERROR = "error";

    private IrStopReason() {
    }
}
