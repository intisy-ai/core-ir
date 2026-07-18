package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.json.TestJsonCodec;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.spi.StreamDecoder;
import io.github.intisy.ai.ir.spi.StreamEncoder;
import io.github.intisy.ai.ir.stream.ContentBlockKind;
import io.github.intisy.ai.ir.stream.ContentBlockStartEvent;
import io.github.intisy.ai.ir.stream.ContentBlockStopEvent;
import io.github.intisy.ai.ir.stream.IrStreamEvent;
import io.github.intisy.ai.ir.stream.MessageDeltaEvent;
import io.github.intisy.ai.ir.stream.MessageStartEvent;
import io.github.intisy.ai.ir.stream.MessageStopEvent;
import io.github.intisy.ai.ir.stream.TextDeltaEvent;
import io.github.intisy.ai.ir.stream.ThinkingDeltaEvent;
import io.github.intisy.ai.ir.stream.ThinkingSignatureEvent;
import io.github.intisy.ai.ir.stream.ToolInputDeltaEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden-vector test for a full streamed Anthropic response: {@code message_start} -> a
 * {@code thinking} block (with a {@code signature_delta}) -> a {@code text} block -> a
 * {@code tool_use} block -> {@code message_delta} -> {@code message_stop}, with {@code ping}
 * frames interspersed (dropped -- they carry no semantic payload). The raw SSE text is fed to
 * the decoder split across two chunks to exercise the cross-chunk line buffering, then every
 * decoded event is re-encoded and checked against the original frame it came from.
 *
 * <p>The {@code message_delta}'s {@code stop_reason} is the exotic {@code pause_turn} (an
 * Anthropic reason with no analog on other vendors) to prove it round-trips losslessly through
 * {@link io.github.intisy.ai.ir.IrStopReason#PAUSE_TURN} rather than falling back to {@code
 * IrStopReason#ERROR} and coming back out as a different string.
 */
class AnthropicStreamRoundTripTest {

    private static String frame(String eventName, String data) {
        return "event: " + eventName + "\ndata: " + data + "\n\n";
    }

    // Frames that produce an IR event, in order (excludes "ping", which does not).
    private static final List<String> EXPECTED_DATA = Arrays.asList(
            "{\"type\":\"message_start\",\"message\":{\"id\":\"msg_01ABC\",\"type\":\"message\","
                    + "\"role\":\"assistant\",\"model\":\"claude-opus-4-8\",\"content\":[],"
                    + "\"stop_reason\":null,\"stop_sequence\":null,"
                    + "\"usage\":{\"input_tokens\":30,\"output_tokens\":0}}}",
            "{\"type\":\"content_block_start\",\"index\":0,\"content_block\":{\"type\":\"thinking\",\"thinking\":\"\"}}",
            "{\"type\":\"content_block_delta\",\"index\":0,"
                    + "\"delta\":{\"type\":\"thinking_delta\",\"thinking\":\"Let me check the weather\"}}",
            "{\"type\":\"content_block_delta\",\"index\":0,"
                    + "\"delta\":{\"type\":\"signature_delta\",\"signature\":\"sig-xyz789\"}}",
            "{\"type\":\"content_block_stop\",\"index\":0}",
            "{\"type\":\"content_block_start\",\"index\":1,\"content_block\":{\"type\":\"text\",\"text\":\"\"}}",
            "{\"type\":\"content_block_delta\",\"index\":1,\"delta\":{\"type\":\"text_delta\",\"text\":\"It's \"}}",
            "{\"type\":\"content_block_delta\",\"index\":1,"
                    + "\"delta\":{\"type\":\"text_delta\",\"text\":\"18C and cloudy.\"}}",
            "{\"type\":\"content_block_stop\",\"index\":1}",
            "{\"type\":\"content_block_start\",\"index\":2,\"content_block\":{\"type\":\"tool_use\","
                    + "\"id\":\"toolu_03\",\"name\":\"get_weather\",\"input\":{}}}",
            "{\"type\":\"content_block_delta\",\"index\":2,"
                    + "\"delta\":{\"type\":\"input_json_delta\",\"partial_json\":\"{\\\"city\\\":\"}}",
            "{\"type\":\"content_block_delta\",\"index\":2,"
                    + "\"delta\":{\"type\":\"input_json_delta\",\"partial_json\":\"\\\"Berlin\\\"}\"}}",
            "{\"type\":\"content_block_stop\",\"index\":2}",
            "{\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\"pause_turn\",\"stop_sequence\":null},"
                    + "\"usage\":{\"output_tokens\":42}}",
            "{\"type\":\"message_stop\"}");

    private static String buildSse() {
        StringBuilder sb = new StringBuilder();
        sb.append(frame("message_start", EXPECTED_DATA.get(0)));
        sb.append(frame("ping", "{\"type\":\"ping\"}"));
        for (int i = 1; i <= 3; i++) sb.append(frame("content_block_delta_or_start", EXPECTED_DATA.get(i)));
        sb.append(frame("content_block_stop", EXPECTED_DATA.get(4)));
        sb.append(frame("content_block_start", EXPECTED_DATA.get(5)));
        sb.append(frame("content_block_delta", EXPECTED_DATA.get(6)));
        sb.append(frame("content_block_delta", EXPECTED_DATA.get(7)));
        sb.append(frame("content_block_stop", EXPECTED_DATA.get(8)));
        sb.append(frame("content_block_start", EXPECTED_DATA.get(9)));
        sb.append(frame("content_block_delta", EXPECTED_DATA.get(10)));
        sb.append(frame("content_block_delta", EXPECTED_DATA.get(11)));
        sb.append(frame("content_block_stop", EXPECTED_DATA.get(12)));
        sb.append(frame("ping", "{\"type\":\"ping\"}"));
        sb.append(frame("message_delta", EXPECTED_DATA.get(13)));
        sb.append(frame("message_stop", EXPECTED_DATA.get(14)));
        return sb.toString();
    }

    @Test
    void streamedResponseRoundTripsFrameByFrame() {
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);
        StreamDecoder decoder = translator.newStreamDecoder();

        String sse = buildSse();
        // Split mid-stream (not on a frame boundary) to exercise cross-chunk line buffering.
        int splitPoint = sse.length() / 2;
        List<IrStreamEvent> events = new ArrayList<>();
        events.addAll(decoder.decode(sse.substring(0, splitPoint)));
        events.addAll(decoder.decode(sse.substring(splitPoint)));

        assertEquals(EXPECTED_DATA.size(), events.size(), "ping frames must be dropped, every other frame kept");

        assertTrue(events.get(0) instanceof MessageStartEvent);
        MessageStartEvent messageStart = (MessageStartEvent) events.get(0);
        assertEquals("msg_01ABC", messageStart.id);
        assertEquals("assistant", messageStart.role);
        assertEquals(30, messageStart.usage.inputTokens);

        assertTrue(events.get(1) instanceof ContentBlockStartEvent);
        assertEquals(ContentBlockKind.THINKING, ((ContentBlockStartEvent) events.get(1)).blockKind);

        assertTrue(events.get(2) instanceof ThinkingDeltaEvent);
        assertEquals("Let me check the weather", ((ThinkingDeltaEvent) events.get(2)).text);

        assertTrue(events.get(3) instanceof ThinkingSignatureEvent);
        assertEquals("sig-xyz789", ((ThinkingSignatureEvent) events.get(3)).signature);

        assertTrue(events.get(4) instanceof ContentBlockStopEvent);

        assertTrue(events.get(9) instanceof ContentBlockStartEvent);
        ContentBlockStartEvent toolStart = (ContentBlockStartEvent) events.get(9);
        assertEquals(ContentBlockKind.TOOL_USE, toolStart.blockKind);
        assertEquals("toolu_03", toolStart.toolUseId);
        assertEquals("get_weather", toolStart.toolName);

        assertTrue(events.get(10) instanceof ToolInputDeltaEvent);
        assertEquals("{\"city\":", ((ToolInputDeltaEvent) events.get(10)).partialJson);

        MessageDeltaEvent messageDelta = (MessageDeltaEvent) events.get(13);
        assertEquals("pause_turn", messageDelta.stopReason);
        assertEquals(42, messageDelta.usage.outputTokens);

        assertTrue(events.get(14) instanceof MessageStopEvent);

        // Re-encode every event and check it reproduces the frame it was decoded from.
        StreamEncoder encoder = translator.newStreamEncoder();
        for (int i = 0; i < events.size(); i++) {
            String reEncoded = encoder.encode(events.get(i));
            Object reparsed = json.parse(extractData(reEncoded));
            assertEquals(json.parse(EXPECTED_DATA.get(i)), reparsed,
                    "re-encoded frame " + i + " must match the original SSE frame");
        }
    }

    private static String extractData(String sseFrameText) {
        for (String line : sseFrameText.split("\n")) {
            if (line.startsWith("data:")) {
                String data = line.substring(5);
                return data.startsWith(" ") ? data.substring(1) : data;
            }
        }
        throw new IllegalArgumentException("no data: line in " + sseFrameText);
    }
}
