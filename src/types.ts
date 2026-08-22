// TypeScript mirror of core-ir's neutral IR types (java/ir), matching the exact field shapes the
// Java *Json helpers (IrRequestJson/IrResponseJson/BlockJson/CommonJson/IrStreamEventJson) produce
// and consume. These are plain data shapes only -- all (de)serialization happens Java-side via the
// translators; nothing here re-implements that logic.

export interface IrUsage {
  inputTokens?: number | null;
  outputTokens?: number | null;
  cacheReadInputTokens?: number | null;
  cacheCreationInputTokens?: number | null;
  // Populated only by vendors with a reasoning-token concept; null for vendors with no derived
  // total or reasoning-token count.
  reasoningTokens?: number | null;
  totalTokens?: number | null;
}

export interface IrThinking {
  enabled: boolean;
  budgetTokens?: number | null;
}

export interface IrTool {
  name: string;
  description?: string | null;
  inputSchema: unknown;
  extensions?: Record<string, unknown> | null;
}

export interface IrToolChoice {
  type: "auto" | "any" | "none" | "tool" | string;
  name?: string | null;
  extensions?: Record<string, unknown> | null;
}

export type BlockKind = "text" | "image" | "tool_use" | "tool_result" | "thinking";

interface BlockCommon {
  cacheControl?: string | null;
  extensions?: Record<string, unknown> | null;
}

export interface TextBlock extends BlockCommon {
  kind: "text";
  text: string;
}

export interface ImageBlock extends BlockCommon {
  kind: "image";
  mediaType: string;
  data?: string | null;
  url?: string | null;
}

export interface ToolUseBlock extends BlockCommon {
  kind: "tool_use";
  id: string;
  name: string;
  input: unknown;
}

export interface ToolResultBlock extends BlockCommon {
  kind: "tool_result";
  toolUseId: string;
  content: Block[];
  isError?: boolean | null;
}

export interface ThinkingBlock extends BlockCommon {
  kind: "thinking";
  text: string;
  signature?: string | null;
}

export type Block = TextBlock | ImageBlock | ToolUseBlock | ToolResultBlock | ThinkingBlock;

export interface IrMessage {
  role: string;
  content: Block[];
  extensions?: Record<string, unknown> | null;
}

export interface IrRequest {
  model: string;
  system?: Block[] | null;
  messages: IrMessage[];
  tools?: IrTool[] | null;
  toolChoice?: IrToolChoice | null;
  maxTokens?: number | null;
  temperature?: number | null;
  topP?: number | null;
  topK?: number | null;
  stopSequences?: string[] | null;
  stream: boolean;
  thinking?: IrThinking | null;
  metadata?: Record<string, unknown> | null;
  extensions?: Record<string, unknown> | null;
}

export type IrStopReason =
  | "end_turn"
  | "max_tokens"
  | "tool_use"
  | "stop_sequence"
  | "pause_turn"
  | "refusal"
  | "error"
  | string;

export interface IrResponse {
  id: string;
  model: string;
  content: Block[];
  stopReason: IrStopReason;
  usage?: IrUsage | null;
  extensions?: Record<string, unknown> | null;
}

// ---- Streaming event model ----------------------------------------------------------------------

export type ContentBlockKind = "text" | "tool_use" | "thinking";

interface StreamEventCommon {
  extensions?: Record<string, unknown> | null;
}

export interface MessageStartEvent extends StreamEventCommon {
  event: "message_start";
  id?: string | null;
  model?: string | null;
  role?: string | null;
  usage?: IrUsage | null;
}

export interface ContentBlockStartEvent extends StreamEventCommon {
  event: "content_block_start";
  index: number;
  blockKind: ContentBlockKind;
  toolUseId?: string | null;
  toolName?: string | null;
}

export interface TextDeltaEvent extends StreamEventCommon {
  event: "text_delta";
  index: number;
  text?: string | null;
}

export interface ThinkingDeltaEvent extends StreamEventCommon {
  event: "thinking_delta";
  index: number;
  text?: string | null;
}

export interface ThinkingSignatureEvent extends StreamEventCommon {
  event: "thinking_signature";
  index: number;
  signature?: string | null;
}

export interface ToolInputDeltaEvent extends StreamEventCommon {
  event: "tool_input_delta";
  index: number;
  partialJson?: string | null;
}

export interface ContentBlockStopEvent extends StreamEventCommon {
  event: "content_block_stop";
  index: number;
}

export interface MessageDeltaEvent extends StreamEventCommon {
  event: "message_delta";
  stopReason?: IrStopReason | null;
  usage?: IrUsage | null;
}

export interface MessageStopEvent extends StreamEventCommon {
  event: "message_stop";
}

export interface ErrorEvent extends StreamEventCommon {
  event: "error";
  errorType?: string | null;
  message?: string | null;
}

export type IrStreamEvent =
  | MessageStartEvent
  | ContentBlockStartEvent
  | TextDeltaEvent
  | ThinkingDeltaEvent
  | ThinkingSignatureEvent
  | ToolInputDeltaEvent
  | ContentBlockStopEvent
  | MessageDeltaEvent
  | MessageStopEvent
  | ErrorEvent;

/**
 * A stream of canonical IR events produced directly by a handler.
 *
 * @remarks
 * Hand-written rather than emitted: `ReadableStream` is a web-platform type with no Java
 * counterpart. Vendor SSE bytes exist only at the wire boundary, encoded by a translator, so this is
 * never a byte stream.
 */
export type IrEventStream = ReadableStream<IrStreamEvent>;

/**
 * Owns one app's wire format: the seam between what an app sends and the canonical IR everything
 * downstream carries.
 *
 * @remarks
 * Hand-written rather than emitted from Java, for the same reason `IrEventStream` above is: every
 * type in it is a web-platform global (`Request`, `Response`) or a TypeScript union, so a Java
 * declaration would carry none of the contract and the annotations would carry all of it. The Java
 * side's equivalent seam is the proxy handler's `HttpRequest`/`HttpResponse` pair, which is a
 * different contract over a different transport abstraction, not this one spelled differently.
 */
export interface FrontDoorCapability {
  /** Decodes an app request into IR, or returns null when the request is not one to route. */
  decode(request: Request): Promise<IrRequest | null>;
  /** Encodes an IR result back into the app's wire format. */
  encode(result: IrResponse | IrEventStream): Promise<Response>;
  /** Rebuilds a wire response from a thrown handler error, or returns null when it cannot. */
  encodeError(error: unknown): Response | null;
}
