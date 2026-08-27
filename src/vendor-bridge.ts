// Shared TS glue every *-translator repo builds its VendorTranslator on: the two TransformStream
// wrappers over a stateful stream handle, and a factory that assembles the full VendorTranslator
// from a module loader plus the six generated (de)serialize functions. Contains no vendor-specific
// naming; each *-translator repo supplies that at its own call site.

import type { IrRequest, IrResponse, IrStreamEvent } from "./generated/ir.js";
import type { VendorHandles, VendorTranslator, WithVendorHandles } from "./translators.js";

export function makeDecodeStream(handle: { decode(chunk: string): string }): TransformStream<Uint8Array | string, IrStreamEvent> {
  const textDecoder = new TextDecoder();
  return new TransformStream({
    transform(chunk, controller) {
      const text = typeof chunk === "string" ? chunk : textDecoder.decode(chunk, { stream: true });
      const events: IrStreamEvent[] = JSON.parse(handle.decode(text));
      for (const event of events) controller.enqueue(event);
    },
  });
}

export function makeEncodeStream(handle: { encode(irEventJson: string): string }): TransformStream<IrStreamEvent, string> {
  return new TransformStream({
    transform(event, controller) {
      const wire = handle.encode(JSON.stringify(event));
      if (wire) controller.enqueue(wire);
    },
  });
}

/** Maps a loaded translator module to its six generated (de)serialize/stream-handle functions. */
export interface VendorTranslatorApi<Mod> {
  decodeRequest(mod: Mod): (wireJson: string) => string;
  encodeRequest(mod: Mod): (irRequestJson: string) => string;
  decodeResponse(mod: Mod): (wireJson: string) => string;
  encodeResponse(mod: Mod): (irResponseJson: string) => string;
  newStreamDecoder(mod: Mod): () => { decode(chunk: string): string };
  newStreamEncoder(mod: Mod): () => { encode(irEventJson: string): string };
}

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
