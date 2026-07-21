package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.UnknownBlock;
import io.github.intisy.ai.ir.json.TestJsonCodec;
import io.github.intisy.ai.ir.spi.JsonCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A content block whose {@code type} this codec has no typed model for (e.g. Anthropic's
 * {@code document} content block, not yet ported here) must round-trip losslessly instead of
 * throwing -- a provider sitting between a real client and a real upstream cannot afford to
 * fail an entire request/response over ONE block it doesn't recognize.
 */
class AnthropicUnknownBlockTest {

    @Test
    void unknownRequestBlockTypeRoundTripsViaUnknownBlock() {
        String wire = "{\"model\":\"claude-opus-4-8\",\"max_tokens\":256,\"messages\":["
                + "{\"role\":\"user\",\"content\":["
                + "{\"type\":\"document\",\"source\":{\"type\":\"url\",\"url\":\"https://x/y.pdf\"}},"
                + "{\"type\":\"text\",\"text\":\"summarize this\"}"
                + "]}],\"stream\":false}";
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrRequest decoded = translator.decodeRequest(wire);
        assertTrue(decoded.messages.get(0).content.get(0) instanceof UnknownBlock);
        UnknownBlock unknown = (UnknownBlock) decoded.messages.get(0).content.get(0);
        assertEquals("document", unknown.raw.get("type"));

        String reEncoded = translator.encodeRequest(decoded);
        assertEquals(json.parse(wire), json.parse(reEncoded),
                "an unrecognized block type must survive decode->IR->encode verbatim");
    }

    @Test
    void unknownResponseBlockTypeRoundTripsViaUnknownBlock() {
        String wire = "{\"id\":\"msg_01\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-opus-4-8\",\"content\":["
                + "{\"type\":\"search_result\",\"url\":\"https://x\",\"title\":\"t\"}"
                + "],\"stop_reason\":\"end_turn\",\"stop_sequence\":null,"
                + "\"usage\":{\"input_tokens\":1,\"output_tokens\":1}}";
        JsonCodec json = new TestJsonCodec();
        AnthropicTranslator translator = new AnthropicTranslator(json);

        IrResponse decoded = translator.decodeResponse(wire);
        assertTrue(decoded.content.get(0) instanceof UnknownBlock);

        String reEncoded = translator.encodeResponse(decoded);
        assertEquals(json.parse(wire), json.parse(reEncoded));
    }
}
