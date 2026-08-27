import type { IrStreamEvent } from "./generated/ir.js";

/**
 * A stream of canonical IR events produced directly by a handler.
 *
 * @remarks
 * The one IR type that is hand-written rather than emitted from the Java: `ReadableStream` is a
 * web-platform type with no Java counterpart, so there is no declaration for the emitter to render.
 * Vendor SSE bytes exist only at the wire boundary, encoded by a translator, so this is never a byte
 * stream.
 */
export type IrEventStream = ReadableStream<IrStreamEvent>;
