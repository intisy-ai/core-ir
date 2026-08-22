package io.github.intisy.ai.ir;

import java.util.List;
import java.util.Map;

/** The canonical, vendor-neutral non-streaming response. */
public final class IrResponse {
    public String id;
    public String model;
    public List<Block> content;
    public String stopReason;
    public IrUsage usage;
    public Map<String, Object> extensions;

    public IrResponse() {
    }
}
