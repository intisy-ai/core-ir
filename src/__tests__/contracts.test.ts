import { execFileSync } from "node:child_process";
import { mkdtempSync, readFileSync, readdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const repo = fileURLToPath(new URL("../..", import.meta.url));

function contractFiles(dir: string): string[] {
  // src/generated also holds the TeaVM bundle, which this emission does not produce.
  return readdirSync(dir).filter((name) => name.startsWith("ir-")).sort();
}

function emit(module: string, moduleDir: string, out: string): void {
  execFileSync(process.execPath, [
    join(repo, "api", "scripts", "emit-dts.mjs"),
    "--java-dir", join(repo, "java"),
    "--module", module,
    "--module-dir", moduleDir,
    "--out", out,
  ], { cwd: repo, stdio: "inherit" });
}

it("keeps the committed declarations identical to what the java emits", () => {
  const scratch = mkdtempSync(join(tmpdir(), "ir-contracts-"));
  emit(":ir", "ir", scratch);
  emit(":ir-contracts", "ir-contracts", scratch);

  const emitted = contractFiles(scratch);
  const committed = contractFiles(join(repo, "src", "generated"));
  expect(emitted).toEqual(committed);
  for (const name of emitted) {
    expect(readFileSync(join(scratch, name), "utf8")).toBe(
      readFileSync(join(repo, "src", "generated", name), "utf8"),
    );
  }
});

function typeCheck(file: string): { ok: boolean; output: string } {
  try {
    execFileSync(process.execPath, [
      join(repo, "node_modules", "typescript", "bin", "tsc"),
      "--noEmit", "--strict", "--target", "ES2022", "--lib", "ES2022,DOM",
      "--module", "esnext", "--moduleResolution", "bundler",
      join(repo, "test", "dts-conformance", file),
    ], { cwd: repo, encoding: "utf8" });
    return { ok: true, output: "" };
  } catch (failure) {
    return { ok: false, output: String((failure as { stdout?: string }).stdout ?? failure) };
  }
}

it("accepts a handler written against the emitted seam", () => {
  const result = typeCheck("positive.ts");
  expect(result.output).toBe("");
  expect(result.ok).toBe(true);
});

it("rejects a handler whose call context is not the declared one", () => {
  const result = typeCheck("negative-handler.ts");
  expect(result.ok).toBe(false);
  expect(result.output).toContain("TS2322");
});
