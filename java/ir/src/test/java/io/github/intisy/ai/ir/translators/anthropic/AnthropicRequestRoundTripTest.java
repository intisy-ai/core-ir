package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.Block;
import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrThinking;
import io.github.intisy.ai.ir.IrToolChoice;
import io.github.intisy.ai.ir.TextBlock;
import io.github.intisy.ai.ir.ThinkingBlock;
import io.github.intisy.ai.ir.ToolResultBlock;
import io.github.intisy.ai.ir.ToolUseBlock;
import io.github.intisy.ai.ir.json.TestJsonCodec;
import io.github.intisy.ai.ir.spi.JsonCodec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden-vector test for {@link AnthropicTranslator#decodeRequest}/{@code encodeRequest}: a
 * real-shaped Anthropic Messages API request exercising system + multi-turn messages (with a
 * plain-string turn) + {@code tool_use}/{@code tool_result} + a {@code thinking} block with a
 * signature + block-level and tool-level {@code cache_control}. Fidelity is asserted by
 * comparing the JSON parsed as maps (per the canonical IR design doc's "Fidelity" section), not
 * raw strings, since key order is not semantically meaningful.
 */
class AnthropicRequestRoundTripTest {

    private static final String GOLDEN_REQUEST = "{"
            + "\"model\":\"claude-opus-4-8\","
            + "\"max_tokens\":4096,"
            + "\"system\":[{\"type\":\"text\",\"text\":\"You are a helpful assistant.\","
            + "\"cache_control\":{\"type\":\"ephemeral\",\"ttl\":\"1h\"}}],"
            + "\"messages\":["
            + "{\"role\":\"user\",\"content\":\"What is the weather in Berlin?\"},"
            + "{\"role\":\"assistant\",\"content\":["
            + "{\"type\":\"text\",\"text\":\"Let me check that for you.\"},"
            + "{\"type\":\"tool_use\",\"id\":\"toolu_01\",\"name\":\"get_weather\",\"input\":{\"city\":\"Berlin\"}}"
            + "]},"
            + "{\"role\":\"user\",\"content\":["
            + "{\"type\":\"tool_result\",\"tool_use_id\":\"toolu_01\",\"content\":\"18C, cloudy\"}"
            + "]},"
            + "{\"role\":\"assistant\",\"content\":["
            + "{\"type\":\"thinking\",\"thinking\":\"The weather is 18C and cloudy; summarize it.\","
            + "\"signature\":\"sig-abc123\"},"
            + "{\"type\":\"text\",\"text\":\"It's 18C and cloudy in Berlin.\"}"
            + "]}"
            + "],"
            + "\"tools\":[{\"name\":\"get_weather\",\"description\":\"Get the current weather for a city\","
            + "\"input_schema\":{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}},"
            + "\"required\":[\"city\"]},\"cache_control\":{\"type\":\"ephemeral\"}}],"
            + "\"tool_choice\":{\"type\":\"auto\"},"
            + "\"temperature\":0.7,"
            + "\"top_p\":0.9,"
            + "\"top_k\":40,"
            + "\"stop_sequences\":[\"STOP\"],"
            + "\"stream\":false,"
            + "\"thinking\":{\"type\":\"enabled\",\"budget_tokens\":2048},"
            + "\"metadata\":{\"user_id\":\"user-123\"}"
            + "}";

    @Test
    void requestRoundTripsToSemanticallyEqualJson() {
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrRequest decoded = translator.decodeRequest(GOLDEN_REQUEST);
        String reEncoded = translator.encodeRequest(decoded);

        assertEquals(json.parse(GOLDEN_REQUEST), json.parse(reEncoded),
                "decode->encode must reproduce a semantically-equal Anthropic request");

        // Spot-check the IR shape.
        assertEquals("claude-opus-4-8", decoded.model);
        assertEquals(4096, decoded.maxTokens);
        assertEquals(0.7, decoded.temperature);
        assertEquals(40, decoded.topK);
        assertEquals(IrToolChoice.Type.AUTO, decoded.toolChoice.type);
        assertEquals(1, decoded.tools.size());
        assertEquals("get_weather", decoded.tools.get(0).name);

        assertEquals(1, decoded.system.size());
        Block systemBlock = decoded.system.get(0);
        assertEquals("ephemeral", systemBlock.cacheControl, "cache_control type survives onto the typed field");
        assertEquals("1h", systemBlock.extensions.get("$cacheControlExtra") instanceof Map
                ? ((Map<?, ?>) systemBlock.extensions.get("$cacheControlExtra")).get("ttl")
                : null, "cache_control's non-type fields (ttl) survive via extensions");

        assertEquals(4, decoded.messages.size());
        List<Block> firstTurn = decoded.messages.get(0).content;
        assertEquals(1, firstTurn.size());
        assertTrue(firstTurn.get(0) instanceof TextBlock);
        assertEquals("What is the weather in Berlin?", ((TextBlock) firstTurn.get(0)).text);

        List<Block> secondTurn = decoded.messages.get(1).content;
        assertTrue(secondTurn.get(1) instanceof ToolUseBlock);
        ToolUseBlock toolUse = (ToolUseBlock) secondTurn.get(1);
        assertEquals("get_weather", toolUse.name);
        assertEquals("toolu_01", toolUse.id);

        List<Block> thirdTurn = decoded.messages.get(2).content;
        assertTrue(thirdTurn.get(0) instanceof ToolResultBlock);
        ToolResultBlock toolResult = (ToolResultBlock) thirdTurn.get(0);
        assertEquals("toolu_01", toolResult.toolUseId);
        assertEquals(1, toolResult.content.size());
        assertEquals("18C, cloudy", ((TextBlock) toolResult.content.get(0)).text);

        List<Block> fourthTurn = decoded.messages.get(3).content;
        assertTrue(fourthTurn.get(0) instanceof ThinkingBlock);
        ThinkingBlock thinkingBlock = (ThinkingBlock) fourthTurn.get(0);
        assertEquals("sig-abc123", thinkingBlock.signature, "thinking signature must survive the round trip");

        IrThinking thinking = decoded.thinking;
        assertTrue(thinking.enabled);
        assertEquals(2048, thinking.budgetTokens);

        assertEquals("user-123", decoded.metadata.get("user_id"));
    }

    @Test
    void toolLevelCacheControlSurvivesViaExtensions() {
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrRequest decoded = translator.decodeRequest(GOLDEN_REQUEST);

        Object toolCacheControl = decoded.tools.get(0).extensions.get("cache_control");
        assertTrue(toolCacheControl instanceof Map);
        assertEquals("ephemeral", ((Map<?, ?>) toolCacheControl).get("type"));
    }

    @Test
    void plainStringSystemRoundTrips() {
        String wire = "{\"model\":\"claude-opus-4-8\",\"max_tokens\":256,"
                + "\"system\":\"be terse\","
                + "\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"stream\":false}";
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrRequest decoded = translator.decodeRequest(wire);
        assertEquals(1, decoded.system.size());
        assertEquals("be terse", ((TextBlock) decoded.system.get(0)).text);

        String reEncoded = translator.encodeRequest(decoded);
        assertEquals(json.parse(wire), json.parse(reEncoded));
        assertEquals("be terse", json.parse(reEncoded) instanceof Map
                ? ((Map<?, ?>) json.parse(reEncoded)).get("system") : null);
    }

    @Test
    void unknownTopLevelFieldsSurviveViaExtensions() {
        String wire = "{\"model\":\"claude-opus-4-8\",\"max_tokens\":256,"
                + "\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"stream\":false,"
                + "\"container\":\"container_abc\",\"service_tier\":\"priority\"}";
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrRequest decoded = translator.decodeRequest(wire);
        assertEquals("container_abc", decoded.extensions.get("container"));
        assertEquals("priority", decoded.extensions.get("service_tier"));

        String reEncoded = translator.encodeRequest(decoded);
        assertEquals(json.parse(wire), json.parse(reEncoded));
    }

    @Test
    void disableParallelToolUseSurvivesOnToolChoice() {
        String wire = "{\"model\":\"claude-opus-4-8\",\"max_tokens\":256,"
                + "\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"stream\":false,"
                + "\"tool_choice\":{\"type\":\"any\",\"disable_parallel_tool_use\":true}}";
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrRequest decoded = translator.decodeRequest(wire);
        assertEquals(Boolean.TRUE, decoded.toolChoice.extensions.get("disable_parallel_tool_use"));
        assertNull(decoded.toolChoice.name);

        String reEncoded = translator.encodeRequest(decoded);
        assertEquals(json.parse(wire), json.parse(reEncoded));
    }
}
