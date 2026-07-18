package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.spi.StreamDecoder;
import io.github.intisy.ai.ir.spi.StreamEncoder;
import io.github.intisy.ai.ir.spi.Translator;

/**
 * Anthropic Messages API {@link Translator}: maps the Anthropic wire format (request, response,
 * and SSE stream) to/from the canonical IR. Reused by both a Claude-Code front-door (app wire
 * &lt;-&gt; IR) and claude-code-auth's own upstream (IR &lt;-&gt; Anthropic), per the canonical IR
 * design doc.
 *
 * <p>No gson, no reflection: JSON (de)serialization goes through the injected {@link JsonCodec},
 * matching the rest of this module's SPI-injection pattern.
 */
public final class AnthropicTranslator implements Translator {
    private final JsonCodec json;

    public AnthropicTranslator(JsonCodec json) {
        this.json = json;
    }

    @Override
    public IrRequest decodeRequest(String wireJson) {
        return AnthropicRequestCodec.decodeRequest(json, wireJson);
    }

    @Override
    public String encodeRequest(IrRequest request) {
        return AnthropicRequestCodec.encodeRequest(json, request);
    }

    @Override
    public IrResponse decodeResponse(String wireJson) {
        return AnthropicResponseCodec.decodeResponse(json, wireJson);
    }

    @Override
    public String encodeResponse(IrResponse response) {
        return AnthropicResponseCodec.encodeResponse(json, response);
    }

    @Override
    public StreamDecoder newStreamDecoder() {
        return new AnthropicStreamDecoder(json);
    }

    @Override
    public StreamEncoder newStreamEncoder() {
        return new AnthropicStreamEncoder(json);
    }
}
