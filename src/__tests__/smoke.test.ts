import { expect, it } from "vitest";
import * as coreIr from "../index.js";

it("barrel imports", () => {
  expect(coreIr).toBeTypeOf("object");
});

it("loads the TeaVM module and round-trips a bare JSON value", async () => {
  const mod = await coreIr.loadCoreIr();
  expect(mod.jsonRoundTrip('{"a":1,"b":"x"}')).toBe('{"a":1,"b":"x"}');
});

it("round-trips an IrRequest through the Java-side (de)serialize helper", async () => {
  const mod = await coreIr.loadCoreIr();
  const wire = JSON.stringify({
    model: "test-model-1",
    messages: [{ role: "user", content: [{ kind: "text", text: "hi" }] }],
    stream: false,
  });
  const roundTripped = mod.irRequestRoundTrip(wire);
  expect(JSON.parse(roundTripped)).toEqual(JSON.parse(wire));
});
