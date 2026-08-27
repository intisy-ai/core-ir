import { execFileSync } from "node:child_process";
import { mkdtempSync, readFileSync, readdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const repo = fileURLToPath(new URL("../..", import.meta.url));

function emit(module: string, extra: string[] = []): string {
  const scratch = mkdtempSync(join(tmpdir(), "core-ir-dts-"));
  execFileSync(process.execPath, [
    join(repo, "node_modules", "@intisy-ai", "api", "scripts", "emit-dts.mjs"),
    "--java-dir", repo,
    "--module", module,
    ...extra,
    "--out", scratch,
  ], { cwd: repo, stdio: "inherit" });
  return scratch;
}

function expectMatchesCommitted(scratch: string, names: string[]): void {
  expect(readdirSync(scratch).sort()).toEqual(names);
  for (const name of names) {
    expect(readFileSync(join(scratch, name), "utf8")).toBe(readFileSync(join(repo, "src", "generated", name), "utf8"));
  }
}

it("keeps the committed ir declarations identical to what the java emits", () => {
  expectMatchesCommitted(emit(":ir", ["--module-dir", "ir"]), ["ir.ts"]);
});

it("leaves no link tag unresolved in the emitted prose", () => {
  // The emitter matches "{@link " with a literal space, so a javadoc link broken across two source
  // lines leaks through verbatim and typedoc, which treats an invalid link as an error, then fails.
  const emitted = readFileSync(join(repo, "src", "generated", "ir.ts"), "utf8");
  expect(emitted.match(/\{@link\s*$/gm)).toBeNull();
});

it("keeps the committed teavm declarations identical to what the java emits", () => {
  expectMatchesCommitted(emit(":teavm"), ["core-ir.teavm.d.ts"]);
});
