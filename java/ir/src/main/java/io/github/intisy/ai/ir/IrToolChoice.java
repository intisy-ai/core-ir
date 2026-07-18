package io.github.intisy.ai.ir;

import java.util.Map;

/**
 * {@code auto|any|none|{name}}. {@code type} holds one of {@link Type#AUTO}/{@link Type#ANY}/
 * {@link Type#NONE}/{@link Type#TOOL}; {@code name} is set only for {@code TOOL}.
 *
 * <p>{@code extensions} carries vendor-specific fields with no neutral home (e.g. Anthropic's
 * {@code disable_parallel_tool_use}).
 */
public final class IrToolChoice {
    public String type;
    public String name;
    public Map<String, Object> extensions;

    public IrToolChoice() {
    }

    public IrToolChoice(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /** {@code type} constants. */
    public static final class Type {
        public static final String AUTO = "auto";
        public static final String ANY = "any";
        public static final String NONE = "none";
        public static final String TOOL = "tool";

        private Type() {
        }
    }
}
