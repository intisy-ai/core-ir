package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;
import java.util.Map;

/**
 * The canonical, vendor-neutral request. A front-door (loader/proxy) builds this from the
 * client's wire format; a handler translates it to its own upstream.
 */
@TsInterface(data = true)
public final class IrRequest {
    /** The requested model identifier, in whatever naming scheme the caller used. */
    public String model;
    /** System-prompt content blocks, or null when none is set. */
    @TsOptional
    @TsNullable
    public List<Block> system;
    /** The conversation turns, in order. */
    public List<IrMessage> messages;
    /** Tools the model may call, or null when none are offered. */
    @TsOptional
    @TsNullable
    public List<IrTool> tools;
    /** Constrains how the model may use {@link #tools}, or null when unspecified. */
    @TsOptional
    @TsNullable
    public IrToolChoice toolChoice;
    /** Upper bound on tokens the model may generate, or null when unspecified. */
    @TsOptional
    @TsNullable
    public Integer maxTokens;
    /** Sampling temperature, or null when unspecified. */
    @TsOptional
    @TsNullable
    public Double temperature;
    /** Nucleus-sampling threshold, or null when unspecified. */
    @TsOptional
    @TsNullable
    public Double topP;
    /** Top-k sampling limit, or null when unspecified. */
    @TsOptional
    @TsNullable
    public Integer topK;
    /** Strings that end generation when produced, or null when none are set. */
    @TsOptional
    @TsNullable
    public List<String> stopSequences;
    /** Whether the response should be delivered as IR stream events rather than a single {@link IrResponse}. */
    public boolean stream;
    /** Extended-thinking configuration, or null when unspecified. */
    @TsOptional
    @TsNullable
    public IrThinking thinking;
    /** Caller-supplied request metadata with no neutral equivalent, or null when none is set. */
    @TsOptional
    @TsNullable
    public Map<String, Object> metadata;
    /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
    @TsOptional
    @TsNullable
    public Map<String, Object> extensions;

    /** Creates a request with no fields set yet. */
    public IrRequest() {
    }
}
