package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.IrStopReason;

/**
 * Anthropic {@code stop_reason} <-> {@link IrStopReason}. Anthropic's own values already match
 * the IR constants string-for-string for all six reasons IR knows about, including {@link
 * IrStopReason#PAUSE_TURN} and {@link IrStopReason#REFUSAL}. Only a genuinely unrecognized future
 * Anthropic reason falls back to {@link IrStopReason#ERROR} going in; {@link IrStopReason#ERROR}
 * has no Anthropic equivalent going back out (mapped to {@code end_turn} as a safe default). The
 * non-streaming response path (see {@code AnthropicResponseCodec}) additionally preserves the
 * exact original string via {@code extensions} regardless of this residual fallback; the
 * streaming path carries the same {@code extensions} bag on {@code IrStreamEvent} now, but does
 * not need it for {@code stop_reason} since every known Anthropic value round-trips through the
 * constants directly.
 */
final class AnthropicStopReason {
    private AnthropicStopReason() {
    }

    static String toIr(String anthropicReason) {
        if (IrStopReason.END_TURN.equals(anthropicReason)) return IrStopReason.END_TURN;
        if (IrStopReason.MAX_TOKENS.equals(anthropicReason)) return IrStopReason.MAX_TOKENS;
        if (IrStopReason.TOOL_USE.equals(anthropicReason)) return IrStopReason.TOOL_USE;
        if (IrStopReason.STOP_SEQUENCE.equals(anthropicReason)) return IrStopReason.STOP_SEQUENCE;
        if (IrStopReason.PAUSE_TURN.equals(anthropicReason)) return IrStopReason.PAUSE_TURN;
        if (IrStopReason.REFUSAL.equals(anthropicReason)) return IrStopReason.REFUSAL;
        return IrStopReason.ERROR;
    }

    static String toAnthropic(String irStopReason) {
        if (IrStopReason.ERROR.equals(irStopReason)) return "end_turn";
        return irStopReason;
    }
}
