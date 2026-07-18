package io.github.intisy.ai.ir;

/** Token accounting, shared by {@link IrResponse} and streaming {@code MessageStart}/{@code MessageDelta}. */
public final class IrUsage {
    public Integer inputTokens;
    public Integer outputTokens;
    public Integer cacheReadInputTokens;
    public Integer cacheCreationInputTokens;

    public IrUsage() {
    }

    public IrUsage(Integer inputTokens, Integer outputTokens, Integer cacheReadInputTokens, Integer cacheCreationInputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.cacheReadInputTokens = cacheReadInputTokens;
        this.cacheCreationInputTokens = cacheCreationInputTokens;
    }
}
