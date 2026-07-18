package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.IrStopReason;

/**
 * Anthropic {@code stop_reason} <-> {@link IrStopReason}. Anthropic's own values already match
 * the IR constants string-for-string for the four reasons IR knows about. Anthropic's
 * {@code pause_turn} and {@code refusal} have no IR equivalent (mapped to {@link
 * IrStopReason#ERROR} going in); {@link IrStopReason#ERROR} has no Anthropic equivalent going
 * back out (mapped to {@code end_turn} as a safe default). The non-streaming response path
 * (see {@code AnthropicResponseCodec}) preserves the exact original string via {@code
 * extensions} regardless of this lossy fallback; the streaming path has no such escape hatch
 * yet (stream events carry no {@code extensions} bag) -- see the translator's class doc.
 */
final class AnthropicStopReason {
    private AnthropicStopReason() {
    }

    static String toIr(String anthropicReason) {
        if (IrStopReason.END_TURN.equals(anthropicReason)) return IrStopReason.END_TURN;
        if (IrStopReason.MAX_TOKENS.equals(anthropicReason)) return IrStopReason.MAX_TOKENS;
        if (IrStopReason.TOOL_USE.equals(anthropicReason)) return IrStopReason.TOOL_USE;
        if (IrStopReason.STOP_SEQUENCE.equals(anthropicReason)) return IrStopReason.STOP_SEQUENCE;
        return IrStopReason.ERROR;
    }

    static String toAnthropic(String irStopReason) {
        if (IrStopReason.ERROR.equals(irStopReason)) return "end_turn";
        return irStopReason;
    }
}
