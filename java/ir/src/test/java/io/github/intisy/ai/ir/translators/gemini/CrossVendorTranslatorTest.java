package io.github.intisy.ai.ir.translators.gemini;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.json.TestJsonCodec;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.translators.anthropic.AnthropicTranslator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves the canonical IR is a real interchange, not a private detail of one translator pair:
 * decode a genuine Anthropic Messages API request, then re-encode the SAME {@link IrRequest} as a
 * Gemini {@code generateContent} body with no vendor-specific glue in between.
 */
class CrossVendorTranslatorTest {

    private static final String ANTHROPIC_WIRE = "{"
            + "\"model\":\"claude-opus-4-8\","
            + "\"max_tokens\":1024,"
            + "\"system\":\"You are a helpful assistant.\","
            + "\"messages\":["
            + "{\"role\":\"user\",\"content\":\"What is the weather in Berlin?\"},"
            + "{\"role\":\"assistant\",\"content\":["
            + "{\"type\":\"tool_use\",\"id\":\"toolu_01\",\"name\":\"get_weather\",\"input\":{\"city\":\"Berlin\"}}"
            + "]},"
            + "{\"role\":\"user\",\"content\":["
            + "{\"type\":\"tool_result\",\"tool_use_id\":\"toolu_01\",\"content\":\"18C, cloudy\"}"
            + "]}"
            + "],"
            + "\"tools\":[{\"name\":\"get_weather\",\"description\":\"Get the current weather for a city\","
            + "\"input_schema\":{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}},"
            + "\"required\":[\"city\"]}}],"
            + "\"temperature\":0.5,"
            + "\"stream\":false"
            + "}";

    @Test
    @SuppressWarnings("unchecked")
    void anthropicRequestTranslatesThroughIrIntoAValidGeminiBody() {
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator anthropic = new AnthropicTranslator(json);
        GeminiTranslator gemini = new GeminiTranslator(json);

        IrRequest ir = anthropic.decodeRequest(ANTHROPIC_WIRE);
        String geminiWire = gemini.encodeRequest(ir);

        Map<String, Object> parsed = (Map<String, Object>) json.parse(geminiWire);

        List<Object> contents = (List<Object>) parsed.get("contents");
        assertEquals(3, contents.size());
        assertEquals("user", ((Map<String, Object>) contents.get(0)).get("role"));
        assertEquals("model", ((Map<String, Object>) contents.get(1)).get("role"),
                "Anthropic 'assistant' must become Gemini 'model'");
        assertEquals("user", ((Map<String, Object>) contents.get(2)).get("role"),
                "Anthropic's tool_result-in-a-user-message convention stays a Gemini 'user' turn");

        Map<String, Object> systemInstruction = (Map<String, Object>) parsed.get("systemInstruction");
        List<Object> systemParts = (List<Object>) systemInstruction.get("parts");
        assertEquals("You are a helpful assistant.", ((Map<String, Object>) systemParts.get(0)).get("text"));

        List<Object> tools = (List<Object>) parsed.get("tools");
        Map<String, Object> toolWrap = (Map<String, Object>) tools.get(0);
        List<Object> functionDeclarations = (List<Object>) toolWrap.get("functionDeclarations");
        assertEquals("get_weather", ((Map<String, Object>) functionDeclarations.get(0)).get("name"));

        Map<String, Object> generationConfig = (Map<String, Object>) parsed.get("generationConfig");
        assertEquals(1024L, ((Number) generationConfig.get("maxOutputTokens")).longValue());
        assertEquals(0.5, ((Number) generationConfig.get("temperature")).doubleValue());

        // The functionCall/functionResponse pair correlates by name, exactly as antigravity-auth's
        // AntigravityFormatBridge.anthropicToGemini pairs a tool_use id to its name up front.
        Map<String, Object> secondTurnPart = (Map<String, Object>) ((List<Object>) ((Map<String, Object>) contents.get(1)).get("parts")).get(0);
        Map<String, Object> functionCall = (Map<String, Object>) secondTurnPart.get("functionCall");
        assertEquals("get_weather", functionCall.get("name"));

        Map<String, Object> thirdTurnPart = (Map<String, Object>) ((List<Object>) ((Map<String, Object>) contents.get(2)).get("parts")).get(0);
        Map<String, Object> functionResponse = (Map<String, Object>) thirdTurnPart.get("functionResponse");
        assertEquals("get_weather", functionResponse.get("name"));
        Map<String, Object> response = (Map<String, Object>) functionResponse.get("response");
        assertEquals("18C, cloudy", response.get("result"));

        // The real Gemini generateContent REST API carries the model in the URL, not the body --
        // out of core-ir's scope (transport concern). This codec still passes IrRequest#model
        // through as an optional top-level field when IR carries one, so it is not silently lost.
        assertEquals("claude-opus-4-8", parsed.get("model"));
    }
}
