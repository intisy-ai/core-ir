package io.github.intisy.ai.ir;

/**
 * Token accounting, shared by {@link IrResponse} and streaming {@code MessageStart}/
 * {@code MessageDelta}. {@code reasoningTokens}/{@code totalTokens} are null for vendors with no
 * such concept (Anthropic folds reasoning into {@code outputTokens} and reports no derived total);
 * Gemini populates both from {@code usageMetadata.thoughtsTokenCount}/{@code totalTokenCount}.
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
