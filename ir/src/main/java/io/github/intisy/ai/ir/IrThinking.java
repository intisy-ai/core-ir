package io.github.intisy.ai.ir;

/** Extended-thinking request config: {@code {enabled, budgetTokens?}}. */
public final class IrThinking {
    public boolean enabled;
    public Integer budgetTokens;

    public IrThinking() {
    }

    public IrThinking(boolean enabled, Integer budgetTokens) {
        this.enabled = enabled;
        this.budgetTokens = budgetTokens;
    }
}
