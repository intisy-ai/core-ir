package io.github.intisy.ai.ir;

import java.util.List;
import java.util.Map;

/**
 * The canonical, vendor-neutral request. A front-door (loader/proxy) builds this from the
 * client's wire format; a provider translates it to its own upstream.
 */
public final class IrRequest {
    public String model;
    public List<Block> system;
    public List<IrMessage> messages;
    public List<IrTool> tools;
    public IrToolChoice toolChoice;
    public Integer maxTokens;
    public Double temperature;
    public Double topP;
    public Integer topK;
    public List<String> stopSequences;
    public boolean stream;
    public IrThinking thinking;
    public Map<String, Object> metadata;
    public Map<String, Object> extensions;

    public IrRequest() {
    }
}
