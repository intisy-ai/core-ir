package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.Block;
import io.github.intisy.ai.ir.IrMessage;
import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrStopReason;
import io.github.intisy.ai.ir.IrThinking;
import io.github.intisy.ai.ir.IrTool;
import io.github.intisy.ai.ir.IrToolChoice;
import io.github.intisy.ai.ir.TextBlock;
import io.github.intisy.ai.ir.ThinkingBlock;
import io.github.intisy.ai.ir.ToolResultBlock;
import io.github.intisy.ai.ir.ToolUseBlock;
import io.github.intisy.ai.ir.spi.JsonCodec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fidelity contract: {@code IrRequest -> JSON -> IrRequest -> JSON} is byte-identical (the
 * "lossless round trip" guarantee from the canonical IR design doc), across a request that
 * exercises every {@link Block} kind plus an {@code extensions} passthrough entry.
 */
class IrRequestRoundTripTest {

    private static IrRequest buildMixedRequest() {
        IrRequest r = new IrRequest();
        r.model = "test-model-1";
        r.stream = false;
        r.maxTokens = 1024;
        r.temperature = 0.7;

        ThinkingBlock thinking = new ThinkingBlock();
        thinking.text = "reasoning about the answer";
        thinking.signature = "sig-abc123";

        ToolUseBlock toolUse = new ToolUseBlock();
        toolUse.id = "call-1";
        toolUse.name = "get_weather";
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("city", "Berlin");
        toolUse.input = input;

        TextBlock text = new TextBlock("hello world");
        Map<String, Object> textExt = new LinkedHashMap<>();
        textExt.put("cache_control_vendor_field", "ephemeral");
        text.extensions = textExt;

        ToolResultBlock toolResult = new ToolResultBlock();
        toolResult.toolUseId = "call-1";
        toolResult.isError = false;
        toolResult.content = new ArrayList<>();
        toolResult.content.add(new TextBlock("18C, cloudy"));

        List<Block> blocks = new ArrayList<>();
        blocks.add(text);
        blocks.add(thinking);
        blocks.add(toolUse);
        blocks.add(toolResult);

        IrMessage msg = new IrMessage("user", blocks);
        r.messages = new ArrayList<>();
        r.messages.add(msg);

        r.system = new ArrayList<>();
        r.system.add(new TextBlock("you are a helpful assistant"));

        IrTool tool = new IrTool();
        tool.name = "get_weather";
        tool.description = "look up the weather for a city";
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        tool.inputSchema = schema;
        r.tools = Arrays.asList(tool);

        r.toolChoice = new IrToolChoice(IrToolChoice.Type.AUTO, null);
        r.thinking = new IrThinking(true, 2048);
        r.stopSequences = Arrays.asList("STOP");

        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("vendor_safety_settings", "block_none");
        r.extensions = ext;

        return r;
    }

    @Test
    void requestRoundTripsThroughJsonLosslessly() {
        JsonCodec json = new TestJsonCodec();
        IrRequest original = buildMixedRequest();

        String firstJson = IrJson.serializeRequest(json, original);
        IrRequest decoded = IrJson.parseRequest(json, firstJson);
        String secondJson = IrJson.serializeRequest(json, decoded);

        assertEquals(firstJson, secondJson, "decode->encode must be byte-identical");

        assertEquals("test-model-1", decoded.model);
        assertEquals(1024, decoded.maxTokens);
        assertEquals(1, decoded.messages.size());

        List<Block> content = decoded.messages.get(0).content;
        assertEquals(4, content.size());

        assertTrue(content.get(0) instanceof TextBlock);
        assertEquals("hello world", ((TextBlock) content.get(0)).text);
        assertEquals("ephemeral", content.get(0).extensions.get("cache_control_vendor_field"));

        assertTrue(content.get(1) instanceof ThinkingBlock);
        ThinkingBlock decodedThinking = (ThinkingBlock) content.get(1);
        assertEquals("sig-abc123", decodedThinking.signature, "thinking signature must survive the round trip");

        assertTrue(content.get(2) instanceof ToolUseBlock);
        ToolUseBlock decodedToolUse = (ToolUseBlock) content.get(2);
        assertEquals("get_weather", decodedToolUse.name);
        assertNotNull(decodedToolUse.input);

        assertTrue(content.get(3) instanceof ToolResultBlock);
        ToolResultBlock decodedResult = (ToolResultBlock) content.get(3);
        assertEquals("call-1", decodedResult.toolUseId);
        assertEquals(1, decodedResult.content.size());

        assertEquals("vendor_safety_settings", decoded.extensions.keySet().iterator().next());
        assertEquals(IrToolChoice.Type.AUTO, decoded.toolChoice.type);
        assertTrue(decoded.thinking.enabled);
        assertEquals(2048, decoded.thinking.budgetTokens);
    }

    @Test
    void stopReasonConstantsAreWireStrings() {
        assertEquals("end_turn", IrStopReason.END_TURN);
        assertEquals("tool_use", IrStopReason.TOOL_USE);
    }
}
