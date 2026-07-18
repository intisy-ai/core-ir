// Hand-authored ambient types for the TeaVM-generated ES module staged into this same directory
// by `npm run build:teavm` (teavm-build.mjs), from java/teavm's CoreIrJs @JSExport surface. The
// generated core-ir.teavm.js itself is gitignored (build output); this .d.ts is committed source
// so tsc can type-check consumers of `loadCoreIr()` without needing the build to have run first.
//
// T1 foundation only: these are round-trip smoke exports proving the pipeline is wired end to
// end. Vendor translator exports land in later sub-projects (SP-2+).
export function jsonRoundTrip(json: string): string;
export function irRequestRoundTrip(wireJson: string): string;
export function irResponseRoundTrip(wireJson: string): string;
export function irStreamEventRoundTrip(wireJson: string): string;
