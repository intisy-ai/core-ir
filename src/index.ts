// Public barrel for core-ir: the IR types are Java+TeaVM single-sourced (see java/ir), consumed
// from TS via the generated ESM loaded by loadCoreIr() below; translators.ts defines the
// VendorTranslator/StreamTranslator contract that each *-translator repo implements over that
// surface for both non-streaming and true-streaming (TransformStream) use.

// Lazily-memoized dynamic import of the TeaVM ESM -- staged to src/generated/ by
// `npm run build:teavm` ahead of tsc.
let modulePromise: Promise<typeof import("./generated/core-ir.teavm.js")> | null = null;

/**
 * Loads the transpiled core-ir module, once per process.
 *
 * @returns the TeaVM ESM holding the JSON codecs the IR types are read and written by.
 * @remarks
 * Memoized rather than re-imported because the module carries its own copy of the Java class
 * library, so a second instance would be both a wasted parse and a second piece of state.
 */
export function loadCoreIr(): Promise<typeof import("./generated/core-ir.teavm.js")> {
  if (!modulePromise) {
    modulePromise = import("./generated/core-ir.teavm.js");
  }
  return modulePromise;
}

export * from "./translators.js";
export * from "./vendor-bridge.js";
export type * from "./event-stream.js";
export type * from "./generated/ir.js";
