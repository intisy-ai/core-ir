// The vendor translator contract every *-translator repo implements over core-ir's neutral IR
// types. core-ir itself ships no vendor translators; each *-translator repo depends on core-ir as a
// published package and implements these interfaces over its own TeaVM export surface.

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

/**
 * Exposes a loaded vendor module's own string functions.
 *
 * Deliberately NOT a member of {@link VendorTranslator}: only a caller driving a synchronous
 * (Java) router needs them, so requiring them of every translator would break every implementor to
 * serve one consumer. Every translator built by {@link makeVendorTranslator} carries this, and a
 * consumer that needs it asks for `VendorTranslator & WithVendorHandles`.
 */
export interface WithVendorHandles {
  /**
   * The vendor module's string entry points, once it has loaded.
   *
   * A Java host cannot await per call the way {@link VendorTranslator}'s members do, so it resolves
   * these once up front. They are the same transpiled functions that interface is built on, which is
   * what keeps it from embedding a second copy of the vendor in its own bundle.
   */
  handles(): Promise<VendorHandles>;
}

/**
 * A loaded vendor module's raw string entry points, all synchronous.
 *
 * Every one takes and returns JSON text, which is the boundary the transpiled translator already
 * exposes; the typed members of {@link VendorTranslator} are wrappers over exactly these.
 */
export interface VendorHandles {
  decodeRequest(wireJson: string): string;
  encodeRequest(irRequestJson: string): string;
  decodeResponse(wireJson: string): string;
  encodeResponse(irResponseJson: string): string;
  newStreamDecoder(): { decode(chunk: string): string };
  newStreamEncoder(): { encode(irEventJson: string): string };
}
