package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.Map;

/**
 * {@code auto|any|none|{name}}. {@code type} holds one of {@link Type#AUTO}/{@link Type#ANY}/
 * {@link Type#NONE}/{@link Type#TOOL}; {@code name} is set only for {@code TOOL}.
 *
 * <p>{@code extensions} carries vendor-specific fields with no neutral home (e.g. a vendor's own
 * {@code disable_parallel_tool_use}).
 */
@TsInterface(data = true)
public final class IrToolChoice {
    /** One of the {@link Type} constants controlling whether and how the model must use tools. */
    public String type;
    /** The tool the model must call, set only when {@link #type} is {@link Type#TOOL}. */
    @TsOptional
    @TsNullable
    public String name;
    /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
    @TsOptional
    @TsNullable
    public Map<String, Object> extensions;

    /** Creates a tool choice with no fields set yet. */
    public IrToolChoice() {
    }

    /**
     * @param type one of the {@link Type} constants.
     * @param name the tool the model must call, or null unless type is {@link Type#TOOL}.
     */
    public IrToolChoice(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /** {@code type} constants. */
    public static final class Type {
        /** The model decides for itself whether to call a tool. */
        public static final String AUTO = "auto";
        /** The model must call some tool, its choice which. */
        public static final String ANY = "any";
        /** The model must not call any tool. */
        public static final String NONE = "none";
        /** The model must call the tool named in {@link IrToolChoice#name}. */
        public static final String TOOL = "tool";

        private Type() {
        }
    }
}
