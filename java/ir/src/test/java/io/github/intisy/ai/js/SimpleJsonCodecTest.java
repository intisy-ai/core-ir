package io.github.intisy.ai.js;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Focused tests for {@link SimpleJsonCodec}'s behavior, mirroring core-proxy's
 * {@code io.github.intisy.ai.js.SimpleJsonCodecTest}: null/empty input, control-character
 * escaping, and whole-number round trips (no trailing decimal point).
 */
class SimpleJsonCodecTest {

    @Test
    void nullAndEmptyInputParseToNull() {
        SimpleJsonCodec codec = new SimpleJsonCodec();
        assertNull(codec.parse(null));
        assertNull(codec.parse(""));
    }

    @Test
    void namedControlEscapesRoundTrip() {
        SimpleJsonCodec codec = new SimpleJsonCodec();
        String original = "a\nb\tc";
        String json = codec.stringify(original);
        assertEquals("\"a\\nb\\tc\"", json);
        assertEquals(original, codec.parse(json));
    }

    @Test
    void wholeNumberRoundTripsWithoutTrailingZero() {
        SimpleJsonCodec codec = new SimpleJsonCodec();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("count", 5L);
        String out = codec.stringify(m);
        assertTrue(out.contains("\"count\":5"), out);
        assertFalse(out.contains("\"count\":5.0"), out);

        Object parsed = codec.parse(out);
        assertTrue(parsed instanceof Map);
        assertEquals(5L, ((Map<?, ?>) parsed).get("count"));
    }
}
