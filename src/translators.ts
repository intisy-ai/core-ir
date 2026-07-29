// The vendor translator contract every *-translator repo implements over core-ir's neutral IR
// types. core-ir itself ships no vendor translators; each *-translator repo nests core-ir as a
// submodule and implements these interfaces over its own TeaVM export surface.

import type { IrRequest, IrResponse, IrStreamEvent } from "./types.js";

/** One IR stream-event decoder/encoder pair over a real Web Streams TransformStream. */
export interface StreamTranslator {
  /** Raw vendor SSE bytes/text in, decoded {@link IrStreamEvent}s out (one enqueue per event). */
  decodeStream(): Promise<TransformStream<Uint8Array | string, IrStreamEvent>>;
  /** {@link IrStreamEvent}s in, the vendor's wire text out (one enqueue per event, empty ones dropped). */
  encodeStream(): Promise<TransformStream<IrStreamEvent, string>>;
}

export interface VendorTranslator extends StreamTranslator {
  decodeRequest(wireJson: string): Promise<IrRequest>;
  encodeRequest(request: IrRequest): Promise<string>;
  decodeResponse(wireJson: string): Promise<IrResponse>;
  encodeResponse(response: IrResponse): Promise<string>;
}
