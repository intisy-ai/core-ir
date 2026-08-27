package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;

/** Extended-thinking request config: {@code {enabled, budgetTokens?}}. */
@TsInterface(data = true)
public final class IrThinking {
    /** Whether extended thinking is requested for this call. */
    public boolean enabled;
    /** Token budget allotted to thinking, or null when unspecified. */
    @TsOptional
    @TsNullable
    public Integer budgetTokens;

    /** Creates a thinking config with everything left at its default: disabled, no budget set. */
    public IrThinking() {
    }

    /**
     * @param enabled whether extended thinking is requested.
     * @param budgetTokens the token budget allotted to thinking, or null when unspecified.
     */
    public IrThinking(boolean enabled, Integer budgetTokens) {
        this.enabled = enabled;
        this.budgetTokens = budgetTokens;
    }
}
