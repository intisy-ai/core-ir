package io.github.intisy.ai.ir;

import java.util.List;
import java.util.Map;

/** The canonical, vendor-neutral non-streaming response. */
public final class IrResponse {
    /** The response identifier assigned by the upstream vendor. */
    public String id;
    /** The model that produced the response, as reported by the upstream vendor. */
    public String model;
    /** The response's content blocks, in order. */
    public List<Block> content;
    /** Why generation stopped; one of the {@link IrStopReason} constants. */
    public String stopReason;
    /** Token accounting for the request/response pair, or null when the vendor reports none. */
    public IrUsage usage;
    /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
    public Map<String, Object> extensions;

    /** Creates a response with no fields set yet. */
    public IrResponse() {
    }
}
