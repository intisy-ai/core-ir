// TS-facing translator API: a thin wrapper over the TeaVM-generated JS (see src/index.ts's
// loadCoreIr()). Non-streaming calls are one round trip through the Java translator per call;
// streaming calls hand back a real TransformStream driven chunk-by-chunk by a stateful Java-side
// handle (newStreamDecoder/newStreamEncoder): all SSE line-buffering and event-building decisions
// run in Java, this shell only owns bytes-in/JSON-out.

import { loadCoreIr } from "./index.js";
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

function makeDecodeStream(handle: { decode(chunk: string): string }): TransformStream<Uint8Array | string, IrStreamEvent> {
  const textDecoder = new TextDecoder();
  return new TransformStream({
    transform(chunk, controller) {
      const text = typeof chunk === "string" ? chunk : textDecoder.decode(chunk, { stream: true });
      const events: IrStreamEvent[] = JSON.parse(handle.decode(text));
      for (const event of events) controller.enqueue(event);
    },
  });
}

function makeEncodeStream(handle: { encode(irEventJson: string): string }): TransformStream<IrStreamEvent, string> {
  return new TransformStream({
    transform(event, controller) {
      const wire = handle.encode(JSON.stringify(event));
      if (wire) controller.enqueue(wire);
    },
  });
}

export const anthropicTranslator: VendorTranslator = {
  async decodeRequest(wireJson) {
    const mod = await loadCoreIr();
    return JSON.parse(mod.anthropicDecodeRequest(wireJson));
  },
  async encodeRequest(request) {
    const mod = await loadCoreIr();
    return mod.anthropicEncodeRequest(JSON.stringify(request));
  },
  async decodeResponse(wireJson) {
    const mod = await loadCoreIr();
    return JSON.parse(mod.anthropicDecodeResponse(wireJson));
  },
  async encodeResponse(response) {
    const mod = await loadCoreIr();
    return mod.anthropicEncodeResponse(JSON.stringify(response));
  },
  async decodeStream() {
    const mod = await loadCoreIr();
    return makeDecodeStream(mod.anthropicNewStreamDecoder());
  },
  async encodeStream() {
    const mod = await loadCoreIr();
    return makeEncodeStream(mod.anthropicNewStreamEncoder());
  },
};

export const geminiTranslator: VendorTranslator = {
  async decodeRequest(wireJson) {
    const mod = await loadCoreIr();
    return JSON.parse(mod.geminiDecodeRequest(wireJson));
  },
  async encodeRequest(request) {
    const mod = await loadCoreIr();
    return mod.geminiEncodeRequest(JSON.stringify(request));
  },
  async decodeResponse(wireJson) {
    const mod = await loadCoreIr();
    return JSON.parse(mod.geminiDecodeResponse(wireJson));
  },
  async encodeResponse(response) {
    const mod = await loadCoreIr();
    return mod.geminiEncodeResponse(JSON.stringify(response));
  },
  async decodeStream() {
    const mod = await loadCoreIr();
    return makeDecodeStream(mod.geminiNewStreamDecoder());
  },
  async encodeStream() {
    const mod = await loadCoreIr();
    return makeEncodeStream(mod.geminiNewStreamEncoder());
  },
};

export const translators = {
  anthropic: anthropicTranslator,
  gemini: geminiTranslator,
};
