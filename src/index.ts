// Public barrel for core-ir. T1 (this foundation) ships only the IR-round-trip smoke surface
// proven through the TeaVM pipeline -- the IR types themselves are Java+TeaVM single-sourced
// (see java/ir), consumed from TS via this generated ESM. Vendor translators (Anthropic/Gemini)
// and their exports land in later sub-projects (SP-2+, see the canonical IR design doc).

// Lazily-memoized dynamic import of the TeaVM ESM -- staged to src/generated/ by
// `npm run build:teavm` ahead of tsc, matching the pattern used by antigravity-auth's
// javaHandle.ts loadOrchestrator().
let modulePromise: Promise<typeof import("./generated/core-ir.teavm.js")> | null = null;

export function loadCoreIr(): Promise<typeof import("./generated/core-ir.teavm.js")> {
  if (!modulePromise) {
    modulePromise = import("./generated/core-ir.teavm.js");
  }
  return modulePromise;
}
