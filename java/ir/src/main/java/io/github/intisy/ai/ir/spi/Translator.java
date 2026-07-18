package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;

/**
 * Symmetric per-vendor translator contract (see the canonical IR design doc's "Translator
 * interface" section). Each vendor (Anthropic, Gemini, ...) implements this to convert its own
 * wire format to/from the canonical IR, for both non-streaming and streaming traffic.
 *
 * <p>Guarantee: {@code decodeRequest(wire) -> encodeRequest} (and the response/stream
 * equivalents) reproduce a semantically-equal payload for the same vendor -- see the design
 * doc's "Fidelity / lossless round-trip" section.
 */
public interface Translator {
    IrRequest decodeRequest(String wireJson);

    String encodeRequest(IrRequest request);

    IrResponse decodeResponse(String wireJson);

    String encodeResponse(IrResponse response);

    /** A fresh, stateful decoder for one streamed connection. */
    StreamDecoder newStreamDecoder();

    /** A fresh, stateful encoder for one streamed connection. */
    StreamEncoder newStreamEncoder();
}
