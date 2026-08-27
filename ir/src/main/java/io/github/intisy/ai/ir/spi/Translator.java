package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;

/**
 * Symmetric per-vendor translator contract. Each vendor implements this to convert its own wire
 * format to/from the canonical IR, for both non-streaming and streaming traffic.
 *
 * <p>Guarantee: {@code decodeRequest(wire) -> encodeRequest} (and the response/stream
 * equivalents) reproduce a semantically-equal payload for the same vendor.
 */
public interface Translator {
    /**
     * @param wireJson the vendor's request wire text.
     * @return the decoded request.
     */
    IrRequest decodeRequest(String wireJson);

    /**
     * @param request the request to encode.
     * @return the vendor's request wire text.
     */
    String encodeRequest(IrRequest request);

    /**
     * @param wireJson the vendor's response wire text.
     * @return the decoded response.
     */
    IrResponse decodeResponse(String wireJson);

    /**
     * @param response the response to encode.
     * @return the vendor's response wire text.
     */
    String encodeResponse(IrResponse response);

    /**
     * @return a fresh, stateful decoder for one streamed connection.
     */
    StreamDecoder newStreamDecoder();

    /**
     * @return a fresh, stateful encoder for one streamed connection.
     */
    StreamEncoder newStreamEncoder();
}
