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

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object o) {
        return o instanceof List ? (List<Object>) o : null;
    }

    public static String asString(Object o) {
        return o instanceof String ? (String) o : null;
    }

    public static Boolean asBoolean(Object o) {
        return o instanceof Boolean ? (Boolean) o : null;
    }

    public static Integer asInt(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : null;
    }

    public static Double asDouble(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : null;
    }
}
