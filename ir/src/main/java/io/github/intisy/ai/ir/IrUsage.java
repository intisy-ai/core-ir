package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;

/**
 * Token accounting, shared by {@link IrResponse} and streaming {@code MessageStart}/
 * {@code MessageDelta}. {@code reasoningTokens}/{@code totalTokens} are null for vendors with no
 * such concept (some vendors fold reasoning into {@code outputTokens} and report no derived total);
 * others populate both from their own separate reasoning/total-token usage fields.
 */
@TsInterface(data = true)
public final class IrUsage {
    /** Tokens in the request's input, or null when the vendor does not report it. */
    @TsOptional
    @TsNullable
    public Integer inputTokens;
    /** Tokens the model generated, or null when the vendor does not report it. */
    @TsOptional
    @TsNullable
    public Integer outputTokens;
    /** Input tokens served from a prompt cache, or null when the vendor does not report it. */
    @TsOptional
    @TsNullable
    public Integer cacheReadInputTokens;
    /** Input tokens written to a prompt cache, or null when the vendor does not report it. */
    @TsOptional
    @TsNullable
    public Integer cacheCreationInputTokens;
    /** Tokens spent on reasoning, or null for vendors with no such concept. */
    @TsOptional
    @TsNullable
    public Integer reasoningTokens;
    /** The vendor's own reported token total, or null for vendors that report none. */
    @TsOptional
    @TsNullable
    public Integer totalTokens;

    /** Creates a usage record with no fields set yet. */
    public IrUsage() {
    }

    /**
     * @param inputTokens tokens in the request's input, or null when the vendor does not report it.
     * @param outputTokens tokens the model generated, or null when the vendor does not report it.
     * @param cacheReadInputTokens input tokens served from a prompt cache, or null when the vendor does not report it.
     * @param cacheCreationInputTokens input tokens written to a prompt cache, or null when the vendor does not report it.
     */
    public IrUsage(Integer inputTokens, Integer outputTokens, Integer cacheReadInputTokens, Integer cacheCreationInputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.cacheReadInputTokens = cacheReadInputTokens;
        this.cacheCreationInputTokens = cacheCreationInputTokens;
    }
}
