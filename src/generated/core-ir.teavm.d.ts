// Hand-authored ambient types for the TeaVM-generated ES module staged into this same directory
// by `npm run build:teavm` (teavm-build.mjs), from java/teavm's CoreIrJs @JSExport surface. The
// generated core-ir.teavm.js itself is gitignored (build output); this .d.ts is committed source
// so tsc can type-check consumers of `loadCoreIr()` without needing the build to have run first.

// Bare round-trip smoke exports proving the pipeline is wired end to end.
export function jsonRoundTrip(json: string): string;
export function irRequestRoundTrip(wireJson: string): string;
export function irResponseRoundTrip(wireJson: string): string;
export function irStreamEventRoundTrip(wireJson: string): string;

// Non-streaming translator exports: vendor wireJson <-> core-ir's own IrRequest/IrResponse
// JSON shape (the same shape IrJson.serialize*/parse* produce -- see src/types.ts).
export function anthropicDecodeRequest(wireJson: string): string;
export function anthropicEncodeRequest(irRequestJson: string): string;
export function anthropicDecodeResponse(wireJson: string): string;
export function anthropicEncodeResponse(irResponseJson: string): string;
export function geminiDecodeRequest(wireJson: string): string;
export function geminiEncodeRequest(irRequestJson: string): string;
export function geminiDecodeResponse(wireJson: string): string;
export function geminiEncodeResponse(irResponseJson: string): string;

// Streaming translator exports: stateful per-connection handles. `decode` takes one raw
// vendor chunk (partial lines/frames are buffered internally, across calls) and returns a JSON
// array of IR stream-event objects completed by that chunk (often empty). `encode` takes one IR
// stream-event's JSON and returns the vendor's wire text for it (an SSE frame, or "" when that
// event has no wire representation for this vendor).
export interface JsStreamDecoderHandle {
  decode(chunk: string): string;
}
export interface JsStreamEncoderHandle {
  encode(irEventJson: string): string;
}
export function anthropicNewStreamDecoder(): JsStreamDecoderHandle;
export function anthropicNewStreamEncoder(): JsStreamEncoderHandle;
export function geminiNewStreamDecoder(): JsStreamDecoderHandle;
export function geminiNewStreamEncoder(): JsStreamEncoderHandle;
