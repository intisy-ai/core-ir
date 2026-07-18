package io.github.intisy.ai.ir;

/**
 * {@code auto|any|none|{name}}. {@code type} holds one of {@link Type#AUTO}/{@link Type#ANY}/
 * {@link Type#NONE}/{@link Type#TOOL}; {@code name} is set only for {@code TOOL}.
 */
public final class IrToolChoice {
    public String type;
    public String name;

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
