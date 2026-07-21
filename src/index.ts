// Public barrel for core-ir: the IR types are Java+TeaVM single-sourced (see java/ir), consumed
// from TS via the generated ESM loaded by loadCoreIr() below; translators.ts wraps that surface in
// a typed, ergonomic API (translators.anthropic/translators.gemini) for both non-streaming and
// true-streaming (TransformStream) use.

// Lazily-memoized dynamic import of the TeaVM ESM -- staged to src/generated/ by
// `npm run build:teavm` ahead of tsc.
let modulePromise: Promise<typeof import("./generated/core-ir.teavm.js")> | null = null;

export function loadCoreIr(): Promise<typeof import("./generated/core-ir.teavm.js")> {
  if (!modulePromise) {
    modulePromise = import("./generated/core-ir.teavm.js");
  }
  return modulePromise;
}

export * from "./translators.js";
export * from "./types.js";
