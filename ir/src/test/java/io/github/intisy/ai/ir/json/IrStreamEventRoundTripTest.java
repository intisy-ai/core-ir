package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.IrUsage;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.stream.ContentBlockStartEvent;
import io.github.intisy.ai.ir.stream.ContentBlockKind;
import io.github.intisy.ai.ir.stream.IrStreamEvent;
import io.github.intisy.ai.ir.stream.MessageStartEvent;
import io.github.intisy.ai.ir.stream.MessageStopEvent;
import io.github.intisy.ai.ir.stream.ThinkingSignatureEvent;
import io.github.intisy.ai.ir.stream.ToolInputDeltaEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A couple of streaming events round trip through JSON, covering the discriminator dispatch for
 * both a field-bearing event ({@link MessageStartEvent}) and a fieldless one
 * ({@link MessageStopEvent}), plus the {@link ThinkingSignatureEvent}/{@link ToolInputDeltaEvent}
 * shapes.
 */
class IrStreamEventRoundTripTest {

    private static <T extends IrStreamEvent> T roundTrip(JsonCodec json, T event) {
        String firstJson = IrJson.serializeStreamEvent(json, event);
        IrStreamEvent decoded = IrJson.parseStreamEvent(json, firstJson);
        String secondJson = IrJson.serializeStreamEvent(json, decoded);
        assertEquals(firstJson, secondJson);
        @SuppressWarnings("unchecked")
        T typed = (T) decoded;
        return typed;
    }

    @Test
    void messageStartRoundTrips() {
        JsonCodec json = new TestJsonCodec();
        MessageStartEvent ev = new MessageStartEvent();
        ev.id = "msg-1";
        ev.model = "test-model-1";
        ev.role = "assistant";
        ev.usage = new IrUsage(50, 0, null, null);

        MessageStartEvent decoded = roundTrip(json, ev);
        assertEquals("msg-1", decoded.id);
        assertEquals(50, decoded.usage.inputTokens);
    }

    @Test
    void contentBlockStartToolUseRoundTrips() {
        JsonCodec json = new TestJsonCodec();
        ContentBlockStartEvent ev = new ContentBlockStartEvent();
        ev.index = 1;
        ev.blockKind = ContentBlockKind.TOOL_USE;
        ev.toolUseId = "call-3";
        ev.toolName = "lookup";

        ContentBlockStartEvent decoded = roundTrip(json, ev);
        assertEquals(1, decoded.index);
        assertEquals("call-3", decoded.toolUseId);
    }

    @Test
    void thinkingSignatureAndToolInputDeltaRoundTrip() {
        JsonCodec json = new TestJsonCodec();

        ThinkingSignatureEvent sig = new ThinkingSignatureEvent();
        sig.index = 0;
        sig.signature = "sig-xyz";
        assertEquals("sig-xyz", roundTrip(json, sig).signature);

        ToolInputDeltaEvent delta = new ToolInputDeltaEvent();
        delta.index = 2;
        delta.partialJson = "{\"city\":\"Ber";
        assertEquals("{\"city\":\"Ber", roundTrip(json, delta).partialJson);
    }

    @Test
    void messageStopHasOnlyTheDiscriminator() {
        JsonCodec json = new TestJsonCodec();
        MessageStopEvent decoded = roundTrip(json, new MessageStopEvent());
        assertTrue(decoded instanceof MessageStopEvent);
    }
}
