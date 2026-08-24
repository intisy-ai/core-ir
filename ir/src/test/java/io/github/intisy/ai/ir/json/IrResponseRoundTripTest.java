package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.IrStopReason;
import io.github.intisy.ai.ir.IrUsage;
import io.github.intisy.ai.ir.TextBlock;
import io.github.intisy.ai.ir.ToolUseBlock;
import io.github.intisy.ai.ir.spi.JsonCodec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@code IrResponse -> JSON -> IrResponse -> JSON} lossless round trip, incl. a tool_use block. */
class IrResponseRoundTripTest {

    @Test
    void responseRoundTripsThroughJsonLosslessly() {
        JsonCodec json = new TestJsonCodec();

        IrResponse r = new IrResponse();
        r.id = "resp-1";
        r.model = "test-model-1";
        r.stopReason = IrStopReason.TOOL_USE;
        r.usage = new IrUsage(120, 45, 10, 0);

        r.content = new ArrayList<>();
        r.content.add(new TextBlock("here is my answer"));
        ToolUseBlock toolUse = new ToolUseBlock();
        toolUse.id = "call-2";
        toolUse.name = "search";
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("query", "weather in Berlin");
        toolUse.input = input;
        r.content.add(toolUse);

        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("vendor_finish_reason", "TOOL_CALL");
        r.extensions = ext;

        String firstJson = IrJson.serializeResponse(json, r);
        IrResponse decoded = IrJson.parseResponse(json, firstJson);
        String secondJson = IrJson.serializeResponse(json, decoded);

        assertEquals(firstJson, secondJson);
        assertEquals("resp-1", decoded.id);
        assertEquals(IrStopReason.TOOL_USE, decoded.stopReason);
        assertEquals(120, decoded.usage.inputTokens);
        assertEquals(2, decoded.content.size());
        assertTrue(decoded.content.get(1) instanceof ToolUseBlock);
        assertEquals("TOOL_CALL", decoded.extensions.get("vendor_finish_reason"));
    }
}
