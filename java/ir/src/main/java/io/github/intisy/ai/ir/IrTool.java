package io.github.intisy.ai.ir;

/** A tool the model may call. {@code inputSchema} is a parsed JSON-schema tree. */
public final class IrTool {
    public String name;
    public String description;
    public Object inputSchema;

    public IrTool() {
    }

    public IrTool(String name, String description, Object inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
}
