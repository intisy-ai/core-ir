package io.github.intisy.ai.ir;

import java.util.Map;

/**
 * A tool the model may call. {@code inputSchema} is a parsed JSON-schema tree.
 *
 * <p>{@code extensions} carries vendor-specific tool fields with no neutral home (e.g. a vendor's
 * own {@code cache_control} on a tool definition), so a translator's round trip stays lossless.
 */
public final class IrTool {
    /** The tool's name, as the model will refer to it in a {@link ToolUseBlock}. */
    public String name;
    /** Human-readable description of what the tool does, or null when none is given. */
    public String description;
    /** The tool's parameters as a parsed JSON-schema tree. */
    public Object inputSchema;
    /** Vendor-specific tool fields with no neutral equivalent, or null when none apply. */
    public Map<String, Object> extensions;

    /** Creates a tool with no fields set yet. */
    public IrTool() {
    }

    /**
     * @param name the tool's name, as the model will refer to it in a {@link ToolUseBlock}.
     * @param description human-readable description of what the tool does, or null when none is given.
     * @param inputSchema the tool's parameters as a parsed JSON-schema tree.
     */
    public IrTool(String name, String description, Object inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
}
