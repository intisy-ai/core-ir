import { describe, expect, it } from "vitest";
import { makeDecodeStream, makeEncodeStream, makeVendorTranslator } from "../vendor-bridge.js";
import type { IrRequest, IrResponse, IrStreamEvent } from "../types.js";

// Reads concurrently with the writes below (a TransformStream's default backpressure stalls a
// second write once the internal queue is full, so the reader must already be pulling).
function collect<T>(readable: ReadableStream<T>): Promise<T[]> {
  const reader = readable.getReader();
  return (async () => {
    const values: T[] = [];
    for (;;) {
      const { value, done } = await reader.read();
      if (done) break;
      values.push(value);
    }
    return values;
  })();
}

describe("makeDecodeStream", () => {
  it("enqueues each event the handle decodes from a chunk", async () => {
    const handle = {
      decode: (chunk: string) => JSON.stringify([{ event: "text_delta", index: 0, text: chunk }]),
    };
    const stream = makeDecodeStream(handle);
    const events = collect(stream.readable);
    const writer = stream.writable.getWriter();
    await writer.write("hi");
    await writer.close();
    expect(await events).toEqual([{ event: "text_delta", index: 0, text: "hi" }]);
  });
});

describe("makeEncodeStream", () => {
  it("enqueues non-empty wire text and drops empty encodes", async () => {
    const handle = {
      encode: (irEventJson: string) => {
        const event = JSON.parse(irEventJson);
        return event.event === "message_stop" ? "" : `wire:${event.event}`;
      },
    };
    const stream = makeEncodeStream(handle);
    const frames = collect(stream.readable);
    const writer = stream.writable.getWriter();
    await writer.write({ event: "message_start" } as IrStreamEvent);
    await writer.write({ event: "message_stop" } as IrStreamEvent);
    await writer.close();
    expect(await frames).toEqual(["wire:message_start"]);
  });
});

describe("makeVendorTranslator", () => {
  interface FakeMod {
    fakeDecodeRequest(wireJson: string): string;
    fakeEncodeRequest(irRequestJson: string): string;
    fakeDecodeResponse(wireJson: string): string;
    fakeEncodeResponse(irResponseJson: string): string;
    fakeNewStreamDecoder(): { decode(chunk: string): string };
    fakeNewStreamEncoder(): { encode(irEventJson: string): string };
  }

  function makeFakeMod(): FakeMod {
    return {
      fakeDecodeRequest: (wireJson) => wireJson,
      fakeEncodeRequest: (irRequestJson) => irRequestJson,
      fakeDecodeResponse: (wireJson) => wireJson,
      fakeEncodeResponse: (irResponseJson) => irResponseJson,
      fakeNewStreamDecoder: () => ({
        decode: (chunk) => JSON.stringify([{ event: "text_delta", index: 0, text: chunk }]),
      }),
      fakeNewStreamEncoder: () => ({
        encode: (irEventJson) => `wire:${JSON.parse(irEventJson).event}`,
      }),
    };
  }

  const translator = makeVendorTranslator(async () => makeFakeMod(), {
    decodeRequest: (mod) => mod.fakeDecodeRequest,
    encodeRequest: (mod) => mod.fakeEncodeRequest,
    decodeResponse: (mod) => mod.fakeDecodeResponse,
    encodeResponse: (mod) => mod.fakeEncodeResponse,
    newStreamDecoder: (mod) => mod.fakeNewStreamDecoder,
    newStreamEncoder: (mod) => mod.fakeNewStreamEncoder,
  });

  it("round-trips a request through decodeRequest->encodeRequest", async () => {
    const request: IrRequest = { model: "m", messages: [], stream: false };
    const decoded = await translator.decodeRequest(JSON.stringify(request));
    expect(decoded).toEqual(request);
    expect(JSON.parse(await translator.encodeRequest(decoded))).toEqual(request);
  });

  it("round-trips a response through decodeResponse->encodeResponse", async () => {
    const response: IrResponse = { id: "r1", model: "m", content: [], stopReason: "end_turn" };
    const decoded = await translator.decodeResponse(JSON.stringify(response));
    expect(decoded).toEqual(response);
    expect(JSON.parse(await translator.encodeResponse(decoded))).toEqual(response);
  });

  it("wires decodeStream/encodeStream through the loaded module's stream handles", async () => {
    const decodeStream = await translator.decodeStream();
    const decoded = collect(decodeStream.readable);
    const decodeWriter = decodeStream.writable.getWriter();
    await decodeWriter.write("hello");
    await decodeWriter.close();
    const events = await decoded;
    expect(events).toEqual([{ event: "text_delta", index: 0, text: "hello" }]);

    const encodeStream = await translator.encodeStream();
    const frames = collect(encodeStream.readable);
    const encodeWriter = encodeStream.writable.getWriter();
    for (const event of events) await encodeWriter.write(event);
    await encodeWriter.close();
    expect(await frames).toEqual(["wire:text_delta"]);
  });

  it("exposes the loaded module's own string handles, the same ones the typed members wrap", async () => {
    const handles = await translator.handles();

    // Synchronous, unlike every typed member: a Java host cannot await per call.
    expect(handles.decodeRequest('{"model":"m"}')).toBe('{"model":"m"}');
    expect(handles.encodeResponse('{"model":"m"}')).toBe('{"model":"m"}');

    // A fresh stream handle per call, because a real encoder is stateful per connection.
    const first = handles.newStreamEncoder();
    const second = handles.newStreamEncoder();
    expect(first).not.toBe(second);
    expect(first.encode(JSON.stringify({ event: "text_delta" }))).toBe("wire:text_delta");
    expect(handles.newStreamDecoder().decode("hi")).toBe(
      JSON.stringify([{ event: "text_delta", index: 0, text: "hi" }]),
    );
  });
});
