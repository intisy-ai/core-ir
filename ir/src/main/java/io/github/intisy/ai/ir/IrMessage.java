package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;
import java.util.Map;

/** One turn in {@link IrRequest#messages}. {@code role} is {@code user|assistant|tool}. */
@TsInterface(data = true)
public final class IrMessage {
    /** Who sent the message: {@code user}, {@code assistant}, or {@code tool}. */
    public String role;
    /** The message's content blocks, in order. */
    public List<Block> content;
    /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
    @TsOptional
    @TsNullable
    public Map<String, Object> extensions;

    /** Creates a message with no role or content set yet. */
    public IrMessage() {
    }

    /**
     * @param role the sender: {@code user}, {@code assistant}, or {@code tool}.
     * @param content the message's content blocks, in order.
     */
    public IrMessage(String role, List<Block> content) {
        this.role = role;
        this.content = content;
    }
}
