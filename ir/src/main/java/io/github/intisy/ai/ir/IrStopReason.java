package io.github.intisy.ai.ir;

/** {@link IrResponse#stopReason} / streaming {@code MessageDelta.stopReason} constants. */
public final class IrStopReason {
    public static final String END_TURN = "end_turn";
    public static final String MAX_TOKENS = "max_tokens";
    public static final String TOOL_USE = "tool_use";
    public static final String STOP_SEQUENCE = "stop_sequence";
    public static final String PAUSE_TURN = "pause_turn";
    public static final String REFUSAL = "refusal";
    public static final String ERROR = "error";

    private IrStopReason() {
    }
}
