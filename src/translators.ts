// The vendor translator contract every *-translator repo implements over core-ir's neutral IR
// types. core-ir itself ships no vendor translators; each *-translator repo depends on core-ir as a
// published package and implements these interfaces over its own TeaVM export surface.

import type { IrRequest, IrResponse, IrStreamEvent } from "./generated/ir.js";

/** One IR stream-event decoder/encoder pair over a real Web Streams TransformStream. */
export interface StreamTranslator {
  /** Raw vendor SSE bytes/text in, decoded {@link IrStreamEvent}s out (one enqueue per event). */
  decodeStream(): Promise<TransformStream<Uint8Array | string, IrStreamEvent>>;
  /** {@link IrStreamEvent}s in, the vendor's wire text out (one enqueue per event, empty ones dropped). */
  encodeStream(): Promise<TransformStream<IrStreamEvent, string>>;
}

/**
 * One vendor's whole wire format, both directions, non-streaming and streaming alike.
 *
 * @remarks
 * Every method is asynchronous because a translator's implementation is a transpiled module loaded
 * on first use, so even a pure string conversion is reached through that load. Requests and
 * responses cross as JSON text rather than parsed objects, since the conversion itself happens in
 * the Java and only its result is handed back.
 */
export interface VendorTranslator extends StreamTranslator {
  /** Vendor request text in, canonical IR out. */
  decodeRequest(wireJson: string): Promise<IrRequest>;
  /** Canonical IR in, vendor request text out. */
  encodeRequest(request: IrRequest): Promise<string>;
  /** Vendor response text in, canonical IR out. */
  decodeResponse(wireJson: string): Promise<IrResponse>;
  /** Canonical IR in, vendor response text out. */
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
  /** Vendor request text in, canonical IR text out. */
  decodeRequest(wireJson: string): string;
  /** Canonical IR text in, vendor request text out. */
  encodeRequest(irRequestJson: string): string;
  /** Vendor response text in, canonical IR text out. */
  decodeResponse(wireJson: string): string;
  /** Canonical IR text in, vendor response text out. */
  encodeResponse(irResponseJson: string): string;
  /** Opens a decoder for one stream; never share the result between two streams. */
  newStreamDecoder(): StreamDecodeHandle;
  /** Opens an encoder for one stream; never share the result between two streams. */
  newStreamEncoder(): StreamEncodeHandle;
}

/**
 * A vendor's stateful decoder for the lifetime of one stream.
 *
 * @remarks
 * Stateful because a vendor's chunk boundary need not be an event boundary, so the handle holds the
 * partial tail between calls. One handle therefore serves exactly one stream.
 */
export interface StreamDecodeHandle {
  /** One chunk of vendor wire text in, a JSON array of the IR events it completed out. */
  decode(chunk: string): string;
}

/**
 * A vendor's stateful encoder for the lifetime of one stream.
 *
 * @remarks
 * Stateful for the same reason its decoder counterpart is: a vendor's framing can depend on what it
 * has already emitted, so one handle serves exactly one stream.
 */
export interface StreamEncodeHandle {
  /** One IR event as JSON text in, the vendor's wire text out, empty when the vendor has no framing for it. */
  encode(irEventJson: string): string;
}
