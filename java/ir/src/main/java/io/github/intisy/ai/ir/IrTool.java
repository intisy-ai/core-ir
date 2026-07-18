package io.github.intisy.ai.ir;

import java.util.Map;

/**
 * A tool the model may call. {@code inputSchema} is a parsed JSON-schema tree.
 *
 * <p>{@code extensions} carries vendor-specific tool fields with no neutral home (e.g.
 * Anthropic's {@code cache_control} on a tool definition), so a translator's round trip stays
 * lossless.
 */
public final class IrTool {
    public String name;
    public String description;
    public Object inputSchema;
    public Map<String, Object> extensions;

    public IrTool() {
    }

    public IrTool(String name, String description, Object inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
}
