package io.github.intisy.ai.ir;

/**
 * Token accounting, shared by {@link IrResponse} and streaming {@code MessageStart}/
 * {@code MessageDelta}. {@code reasoningTokens}/{@code totalTokens} are null for vendors with no
 * such concept (some vendors fold reasoning into {@code outputTokens} and report no derived total);
 * others populate both from their own separate reasoning/total-token usage fields.
 */
public final class IrUsage {
    public Integer inputTokens;
    public Integer outputTokens;
    public Integer cacheReadInputTokens;
    public Integer cacheCreationInputTokens;
    public Integer reasoningTokens;
    public Integer totalTokens;

    public IrUsage() {
    }

    public IrUsage(Integer inputTokens, Integer outputTokens, Integer cacheReadInputTokens, Integer cacheCreationInputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.cacheReadInputTokens = cacheReadInputTokens;
        this.cacheCreationInputTokens = cacheCreationInputTokens;
    }
}
