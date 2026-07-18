import { describe, expect, it } from "vitest";
import { translators } from "../translators.js";
import type { IrStreamEvent } from "../types.js";

describe("anthropic translator", () => {
  it("round-trips a request through decode->encode", async () => {
    const wire = JSON.stringify({
      model: "claude-opus-4-8",
      max_tokens: 256,
      messages: [{ role: "user", content: "What is the weather in Berlin?" }],
      stream: false,
    });

    const ir = await translators.anthropic.decodeRequest(wire);
    expect(ir.model).toBe("claude-opus-4-8");
    expect(ir.maxTokens).toBe(256);
    expect(ir.messages).toHaveLength(1);

    const reEncoded = await translators.anthropic.encodeRequest(ir);
    expect(JSON.parse(reEncoded)).toEqual(JSON.parse(wire));
  });

  it("streams a full SSE response through decodeStream -> IR events -> encodeStream", async () => {
    const sse =
      'event: message_start\ndata: {"type":"message_start","message":{"id":"msg_1","type":"message",' +
      '"role":"assistant","model":"claude-opus-4-8","content":[],"stop_reason":null,' +
      '"stop_sequence":null,"usage":{"input_tokens":10,"output_tokens":0}}}\n\n' +
      'event: content_block_start\ndata: {"type":"content_block_start","index":0,' +
      '"content_block":{"type":"text","text":""}}\n\n' +
      'event: content_block_delta\ndata: {"type":"content_block_delta","index":0,' +
      '"delta":{"type":"text_delta","text":"Hello"}}\n\n' +
      'event: content_block_delta\ndata: {"type":"content_block_delta","index":0,' +
      '"delta":{"type":"text_delta","text":" there"}}\n\n' +
      'event: content_block_stop\ndata: {"type":"content_block_stop","index":0}\n\n' +
      'event: message_delta\ndata: {"type":"message_delta",' +
      '"delta":{"stop_reason":"end_turn","stop_sequence":null},"usage":{"output_tokens":5}}\n\n' +
      'event: message_stop\ndata: {"type":"message_stop"}\n\n';

    // Split arbitrarily mid-frame to exercise cross-chunk buffering (proves true streaming, not
    // buffer-then-parse: the Java StreamDecoder owns the line/frame buffering internally).
    const splitPoint = Math.floor(sse.length / 2);
    const chunks = [sse.slice(0, splitPoint), sse.slice(splitPoint)];

    const decodeStream = await translators.anthropic.decodeStream();
    const writer = decodeStream.writable.getWriter();
    const reader = decodeStream.readable.getReader();
    const encoder = new TextEncoder();

    const events: IrStreamEvent[] = [];
    const drain = (async () => {
      for (;;) {
        const { value, done } = await reader.read();
        if (done) break;
        events.push(value);
      }
    })();

    for (const chunk of chunks) await writer.write(encoder.encode(chunk));
    await writer.close();
    await drain;

    expect(events.map((e) => e.event)).toEqual([
      "message_start",
      "content_block_start",
      "text_delta",
      "text_delta",
      "content_block_stop",
      "message_delta",
      "message_stop",
    ]);

    const encodeStream = await translators.anthropic.encodeStream();
    const encWriter = encodeStream.writable.getWriter();
    const encReader = encodeStream.readable.getReader();
    const frames: string[] = [];
    const encDrain = (async () => {
      for (;;) {
        const { value, done } = await encReader.read();
        if (done) break;
        frames.push(value);
      }
    })();
    for (const event of events) await encWriter.write(event);
    await encWriter.close();
    await encDrain;

    // Re-decode the re-encoded SSE and check it reaches the same event sequence.
    const reDecodeStream = await translators.anthropic.decodeStream();
    const reWriter = reDecodeStream.writable.getWriter();
    const reReader = reDecodeStream.readable.getReader();
    const reEvents: IrStreamEvent[] = [];
    const reDrain = (async () => {
      for (;;) {
        const { value, done } = await reReader.read();
        if (done) break;
        reEvents.push(value);
      }
    })();
    for (const frame of frames) await reWriter.write(frame);
    await reWriter.close();
    await reDrain;

    expect(reEvents.map((e) => e.event)).toEqual(events.map((e) => e.event));
  });
});

describe("gemini translator", () => {
  it("round-trips a request through decode->encode", async () => {
    const wire = JSON.stringify({
      contents: [{ role: "user", parts: [{ text: "What is the weather in Berlin?" }] }],
      generationConfig: { maxOutputTokens: 256, temperature: 0.5 },
    });

    const ir = await translators.gemini.decodeRequest(wire);
    expect(ir.maxTokens).toBe(256);
    expect(ir.temperature).toBe(0.5);
    expect(ir.messages).toHaveLength(1);

    const reEncoded = await translators.gemini.encodeRequest(ir);
    expect(JSON.parse(reEncoded)).toEqual(JSON.parse(wire));
  });

  it("round-trips a response with reasoning/total token usage", async () => {
    const wire = JSON.stringify({
      candidates: [{ content: { role: "model", parts: [{ text: "18C, cloudy" }] }, finishReason: "STOP", index: 0 }],
      usageMetadata: { promptTokenCount: 10, candidatesTokenCount: 4, totalTokenCount: 14, thoughtsTokenCount: 3 },
    });

    const ir = await translators.gemini.decodeResponse(wire);
    expect(ir.usage?.reasoningTokens).toBe(3);
    expect(ir.usage?.totalTokens).toBe(14);

    const reEncoded = await translators.gemini.encodeResponse(ir);
    expect(JSON.parse(reEncoded)).toEqual(JSON.parse(wire));
  });
});
