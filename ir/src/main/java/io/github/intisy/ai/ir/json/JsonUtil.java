package io.github.intisy.ai.ir.json;

import java.util.List;
import java.util.Map;

/**
 * Narrowing helpers over the {@code JsonCodec} parsed shape ({@code Map}/{@code List}/
 * {@code String}/{@code Number}/{@code Boolean}/{@code null}). No reflection, no gson: every
 * conversion in this package hand-rolls the mapping between {@code Map<String,Object>} and POJOs
 * through these helpers so the code stays transpilable.
 */
public final class JsonUtil {
    private JsonUtil() {
    }

    /**
     * @param o the value to narrow.
     * @return o as a map, or null when it is not a {@code Map}.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    /**
     * @param o the value to narrow.
     * @return o as a list, or null when it is not a {@code List}.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object o) {
        return o instanceof List ? (List<Object>) o : null;
    }

    /**
     * @param o the value to narrow.
     * @return o as a string, or null when it is not a {@code String}.
     */
    public static String asString(Object o) {
        return o instanceof String ? (String) o : null;
    }

    /**
     * @param o the value to narrow.
     * @return o as a boolean, or null when it is not a {@code Boolean}.
     */
    public static Boolean asBoolean(Object o) {
        return o instanceof Boolean ? (Boolean) o : null;
    }

    /**
     * @param o the value to narrow.
     * @return o truncated to an int, or null when it is not a {@code Number}.
     */
    public static Integer asInt(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : null;
    }

    /**
     * @param o the value to narrow.
     * @return o widened to a double, or null when it is not a {@code Number}.
     */
    public static Double asDouble(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : null;
    }
}
