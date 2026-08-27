package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;
import io.github.intisy.ai.tsemit.TsVocabulary;
import java.util.List;
import java.util.Map;

/** The canonical, vendor-neutral non-streaming response. */
@TsInterface(data = true)
public final class IrResponse {
    /** The response identifier assigned by the upstream vendor. */
    public String id;
    /** The model that produced the response, as reported by the upstream vendor. */
    public String model;
    /** The response's content blocks, in order. */
    public List<Block> content;
    /** Why generation stopped; one of the {@link IrStopReason} constants. */
    @TsVocabulary(IrStopReason.class)
    public String stopReason;
    /** Token accounting for the request/response pair, or null when the vendor reports none. */
    @TsOptional
    @TsNullable
    public IrUsage usage;
    /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
    @TsOptional
    @TsNullable
    public Map<String, Object> extensions;

    /** Creates a response with no fields set yet. */
    public IrResponse() {
    }
}
