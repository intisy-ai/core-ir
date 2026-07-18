package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.IrStopReason;
import io.github.intisy.ai.ir.TextBlock;
import io.github.intisy.ai.ir.ToolUseBlock;
import io.github.intisy.ai.ir.json.TestJsonCodec;
import io.github.intisy.ai.ir.spi.JsonCodec;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden-vector test for {@link AnthropicTranslator#decodeResponse}/{@code encodeResponse}: a
 * real-shaped Anthropic response with text + {@code tool_use} content and full cache-aware usage.
 */
class AnthropicResponseRoundTripTest {

    private static final String GOLDEN_RESPONSE = "{"
            + "\"id\":\"msg_01XYZ\","
            + "\"type\":\"message\","
            + "\"role\":\"assistant\","
            + "\"model\":\"claude-opus-4-8\","
            + "\"content\":["
            + "{\"type\":\"text\",\"text\":\"It's 18C and cloudy in Berlin.\"},"
            + "{\"type\":\"tool_use\",\"id\":\"toolu_02\",\"name\":\"get_weather\",\"input\":{\"city\":\"Berlin\"}}"
            + "],"
            + "\"stop_reason\":\"tool_use\","
            + "\"stop_sequence\":null,"
            + "\"usage\":{\"input_tokens\":512,\"output_tokens\":128,"
            + "\"cache_creation_input_tokens\":100,\"cache_read_input_tokens\":50}"
            + "}";

    @Test
    void responseRoundTripsToSemanticallyEqualJson() {
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrResponse decoded = translator.decodeResponse(GOLDEN_RESPONSE);
        String reEncoded = translator.encodeResponse(decoded);

        assertEquals(json.parse(GOLDEN_RESPONSE), json.parse(reEncoded),
                "decode->encode must reproduce a semantically-equal Anthropic response");

        assertEquals("msg_01XYZ", decoded.id);
        assertEquals("claude-opus-4-8", decoded.model);
        assertEquals(IrStopReason.TOOL_USE, decoded.stopReason);
        assertEquals(2, decoded.content.size());
        assertTrue(decoded.content.get(0) instanceof TextBlock);
        assertTrue(decoded.content.get(1) instanceof ToolUseBlock);

        assertEquals(512, decoded.usage.inputTokens);
        assertEquals(128, decoded.usage.outputTokens);
        assertEquals(100, decoded.usage.cacheCreationInputTokens);
        assertEquals(50, decoded.usage.cacheReadInputTokens);
    }

    @Test
    void unmappedStopReasonSurvivesViaExtensions() {
        String wire = "{\"id\":\"msg_02\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-fable-5\",\"content\":[{\"type\":\"text\",\"text\":\"...\"}],"
                + "\"stop_reason\":\"pause_turn\",\"stop_sequence\":null,"
                + "\"usage\":{\"input_tokens\":10,\"output_tokens\":5}}";
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrResponse decoded = translator.decodeResponse(wire);
        // "pause_turn" has no neutral IrStopReason equivalent -- falls back to ERROR for typed
        // consumers, but the exact original string still round-trips via extensions.
        assertEquals(IrStopReason.ERROR, decoded.stopReason);

        String reEncoded = translator.encodeResponse(decoded);
        Object reparsed = json.parse(reEncoded);
        assertTrue(reparsed instanceof Map);
        assertEquals("pause_turn", ((Map<?, ?>) reparsed).get("stop_reason"),
                "the exact Anthropic stop_reason string must survive even though IR has no matching constant");
        assertEquals(json.parse(wire), reparsed);
    }
}
