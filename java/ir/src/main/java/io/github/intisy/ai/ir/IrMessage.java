package io.github.intisy.ai.ir;

import java.util.List;
import java.util.Map;

/** One turn in {@link IrRequest#messages}. {@code role} is {@code user|assistant|tool}. */
public final class IrMessage {
    public String role;
    public List<Block> content;
    public Map<String, Object> extensions;

    public IrMessage() {
    }

    public IrMessage(String role, List<Block> content) {
        this.role = role;
        this.content = content;
    }
}
