// Shared TS glue every *-translator repo builds its VendorTranslator on: the two TransformStream
// wrappers over a stateful stream handle, and a factory that assembles the full VendorTranslator
// from a module loader plus the six generated (de)serialize functions. Contains no vendor-specific
// naming; each *-translator repo supplies that at its own call site.

import type { IrRequest, IrResponse, IrStreamEvent } from "./generated/ir.js";
import type {
  StreamDecodeHandle,
  StreamEncodeHandle,
  VendorHandles,
  VendorTranslator,
  WithVendorHandles,
} from "./translators.js";

/**
 * Wraps a stateful vendor decode handle as a stream of canonical IR events.
 *
 * @param handle the vendor's decoder, which returns a JSON array of the events one chunk completed.
 * @returns a transform from vendor SSE bytes or text to IR events, one enqueue per event.
 * @remarks
 * The handle is stateful because a vendor chunk boundary need not be an event boundary, so it holds
 * the partial tail between calls and a caller must not share one handle between two streams.
 */
export function makeDecodeStream(handle: StreamDecodeHandle): TransformStream<Uint8Array | string, IrStreamEvent> {
  const textDecoder = new TextDecoder();
  return new TransformStream({
    transform(chunk, controller) {
      const text = typeof chunk === "string" ? chunk : textDecoder.decode(chunk, { stream: true });
      const events: IrStreamEvent[] = JSON.parse(handle.decode(text));
      for (const event of events) controller.enqueue(event);
    },
  });
}

/**
 * Wraps a stateful vendor encode handle as a stream of that vendor's wire text.
 *
 * @param handle the vendor's encoder, which returns the wire text one IR event produces.
 * @returns a transform from IR events to vendor wire text.
 * @remarks
 * An event that the vendor's format has no representation for encodes to the empty string, which is
 * dropped rather than enqueued, so a neutral event with no vendor equivalent costs nothing on the
 * wire instead of emitting a blank frame.
 */
export function makeEncodeStream(handle: StreamEncodeHandle): TransformStream<IrStreamEvent, string> {
  return new TransformStream({
    transform(event, controller) {
      const wire = handle.encode(JSON.stringify(event));
      if (wire) controller.enqueue(wire);
    },
  });
}

/** Maps a loaded translator module to its six generated (de)serialize/stream-handle functions. */
export interface VendorTranslatorApi<Mod> {
  /** The module's vendor-request-to-IR function. */
  decodeRequest(mod: Mod): (wireJson: string) => string;
  /** The module's IR-to-vendor-request function. */
  encodeRequest(mod: Mod): (irRequestJson: string) => string;
  /** The module's vendor-response-to-IR function. */
  decodeResponse(mod: Mod): (wireJson: string) => string;
  /** The module's IR-to-vendor-response function. */
  encodeResponse(mod: Mod): (irResponseJson: string) => string;
  /** The module's factory for a one-stream decoder. */
  newStreamDecoder(mod: Mod): () => StreamDecodeHandle;
  /** The module's factory for a one-stream encoder. */
  newStreamEncoder(mod: Mod): () => StreamEncodeHandle;
}

/**
 * Assembles a whole {@link VendorTranslator} from a module loader and that module's six entry points.
 *
 * @param load resolves the transpiled vendor module, memoized by the caller if it is to load once.
 * @param api names the six functions on that module, since each vendor exports its own names.
 * @returns the translator, carrying {@link WithVendorHandles} so a Java host can resolve the raw
 * synchronous entry points once instead of awaiting per call.
 * @remarks
 * This is the whole of what a `*-translator` repo needs beyond its own transpiled module, which is
 * why the glue lives here rather than being written once per vendor.
 */
export function makeVendorTranslator<Mod>(load: () => Promise<Mod>, api: VendorTranslatorApi<Mod>): VendorTranslator & WithVendorHandles {
  return {
    async decodeRequest(wireJson: string): Promise<IrRequest> {
      const mod = await load();
      return JSON.parse(api.decodeRequest(mod)(wireJson));
    },
    async encodeRequest(request: IrRequest): Promise<string> {
      const mod = await load();
      return api.encodeRequest(mod)(JSON.stringify(request));
    },
    async decodeResponse(wireJson: string): Promise<IrResponse> {
      const mod = await load();
      return JSON.parse(api.decodeResponse(mod)(wireJson));
    },
    async encodeResponse(response: IrResponse): Promise<string> {
      const mod = await load();
      return api.encodeResponse(mod)(JSON.stringify(response));
    },
    async decodeStream() {
      const mod = await load();
      return makeDecodeStream(api.newStreamDecoder(mod)());
    },
    async encodeStream() {
      const mod = await load();
      return makeEncodeStream(api.newStreamEncoder(mod)());
    },
    async handles(): Promise<VendorHandles> {
      const mod = await load();
      return {
        decodeRequest: api.decodeRequest(mod),
        encodeRequest: api.encodeRequest(mod),
        decodeResponse: api.decodeResponse(mod),
        encodeResponse: api.encodeResponse(mod),
        newStreamDecoder: api.newStreamDecoder(mod),
        newStreamEncoder: api.newStreamEncoder(mod),
      };
    },
  };
}
