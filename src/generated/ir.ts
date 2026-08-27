// Generated from Java sources. Do not edit.

import type { IrEventStream } from "../event-stream.js";
import type { Logger, Store } from "@intisy-ai/api/contract";

/**
 * A content block whose `type` discriminator a translator does not recognize (e.g. a
 * vendor content type added after this codec was written, such as a `document` block some
 * vendor introduces). Rather than fail the whole decode, a translator stashes the ENTIRE raw block
 * verbatim in `raw` so `decode(wire)->IR->encode(wire)` stays lossless for content this
 * codec has no typed model for yet, the same "extensions bag" philosophy as
 * a block's own `extensions`, just for a whole block instead of one unknown field.
 */
export interface UnknownBlock {
  /** Vendor-specific cache-control hint carried verbatim, or null when the vendor sets none. */
  cacheControl?: string | null;
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** The JSON discriminator identifying the concrete subclass; one of the {@link BlockKind} constants. */
  kind: "unknown";
  /** The entire original wire block, preserved verbatim so a translator can re-emit it unchanged. */
  raw: Record<string, unknown>;
}

/**
 * A tool the model may call. `inputSchema` is a parsed JSON-schema tree.
 *
 *
 * `extensions` carries vendor-specific tool fields with no neutral home (e.g. a vendor's
 * own `cache_control` on a tool definition), so a translator's round trip stays lossless.
 */
export interface IrTool {
  /** Human-readable description of what the tool does, or null when none is given. */
  description?: string | null;
  /** Vendor-specific tool fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** The tool's parameters as a parsed JSON-schema tree. */
  inputSchema: unknown;
  /** The tool's name, as the model will refer to it in a {@link ToolUseBlock}. */
  name: string;
}

/**
 * An in-stream error. Named `ErrorEvent` (not `Error`) to avoid shadowing
 * `java.lang.Error`.
 */
export interface ErrorEvent {
  /** The vendor's own error-type string, or null when the vendor does not report one. */
  errorType?: string | null;
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "error";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** Human-readable error message. */
  message?: string | null;
}

/**
 * Answers one id's requests in canonical IR, never in any app's or vendor's wire format.
 *
 * @remarks
 * Declares nothing `ServiceLoader`-specific and nothing JVM-only, so one interface
 * serves both discovery routes: a JVM host loads implementations through
 * `ServiceLoader.load(IrHandler.class)`, while a transpiled host has no classpath to scan and
 * instead constructs its handler directly and registers it under `id()`.
 */
export interface IrHandler {
  /**
   * Serves an already-decoded request.
   *
   * @param request - the request to serve.
   * @param ctx - the per-call context this handler was invoked with.
   * @returns the completed response.
   * @throws HandleIrException for a non-2xx upstream outcome, so the caller can rebuild the
   * response it describes. Any other throw is an unexpected failure.
   */
  handleIr(request: IrRequest, ctx: HandlerCtx): Promise<IrResponse | IrEventStream>;
  /** The id a routing chain names to reach this handler. */
  readonly id: string;
}

/**
 * Base of the canonical streaming event hierarchy: {@link MessageStartEvent},
 * {@link ContentBlockStartEvent}, {@link TextDeltaEvent}, {@link ThinkingDeltaEvent},
 * {@link ThinkingSignatureEvent}, {@link ToolInputDeltaEvent}, {@link ContentBlockStopEvent},
 * {@link MessageDeltaEvent}, {@link MessageStopEvent}, {@link ErrorEvent}. A vendor's
 * `StreamDecoder` maps its SSE chunks to these; its `StreamEncoder` maps these back.
 *
 *
 * `event` is the JSON discriminator (`IrEventType`). `extensions` carries
 * vendor-specific passthrough with no neutral home, the same role as
 * a content block's own `extensions`, so a translator's streaming decode-then-encode round
 * trip stays semantically lossless.
 */
export type IrStreamEvent = ContentBlockStartEvent | ContentBlockStopEvent | ErrorEvent | MessageDeltaEvent | MessageStartEvent | MessageStopEvent | TextDeltaEvent | ThinkingDeltaEvent | ThinkingSignatureEvent | ToolInputDeltaEvent;

/**
 * Base of the content-block hierarchy: {@link TextBlock}, {@link ImageBlock},
 * {@link ToolUseBlock}, {@link ToolResultBlock}, {@link ThinkingBlock}. Chosen as the most
 * expressive superset across vendor content models -- this is a purpose-built neutral shape, not
 * adopting any single vendor's shape.
 *
 *
 * `kind` is the JSON discriminator ({@link BlockKind}). `cacheControl` and
 * `extensions` carry vendor-specific passthrough with no neutral home (e.g. a vendor's own
 * `cache_control` field), so a translator's `decode(wire)->IR->encode(wire)` round trip
 * stays semantically lossless.
 */
export type Block = ImageBlock | TextBlock | ThinkingBlock | ToolResultBlock | ToolUseBlock | UnknownBlock;

/**
 * Extended/reasoning-thinking content. `signature` is the vendor's opaque verification
 * token (e.g. a thinking signature some vendors attach) -- carried verbatim so a translator can
 * restore it on re-encode without needing to understand it.
 */
export interface ThinkingBlock {
  /** Vendor-specific cache-control hint carried verbatim, or null when the vendor sets none. */
  cacheControl?: string | null;
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** The JSON discriminator identifying the concrete subclass; one of the {@link BlockKind} constants. */
  kind: "thinking";
  /** The vendor's opaque verification token, carried verbatim, or null when the vendor issues none. */
  signature?: string | null;
  /** The reasoning content. */
  text: string;
}

/**
 * Opens a content block at `index`. `blockKind` is one of {@link ContentBlockKind};
 * `toolUseId`/`toolName` are set only when `blockKind` is
 * `tool_use`.
 */
export interface ContentBlockStartEvent {
  /** One of the {@link ContentBlockKind} constants. */
  blockKind: ContentBlockKind;
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "content_block_start";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** Position of the content block within the message. */
  index: number;
  /** The tool's name, set only when `blockKind` is `tool_use`. */
  toolName?: string | null;
  /** The tool call's id, set only when `blockKind` is `tool_use`. */
  toolUseId?: string | null;
}

/**
 * The canonical, vendor-neutral request. A front-door (loader/proxy) builds this from the
 * client's wire format; a handler translates it to its own upstream.
 */
export interface IrRequest {
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** Upper bound on tokens the model may generate, or null when unspecified. */
  maxTokens?: number | null;
  /** The conversation turns, in order. */
  messages: IrMessage[];
  /** Caller-supplied request metadata with no neutral equivalent, or null when none is set. */
  metadata?: Record<string, unknown> | null;
  /** The requested model identifier, in whatever naming scheme the caller used. */
  model: string;
  /** Strings that end generation when produced, or null when none are set. */
  stopSequences?: string[] | null;
  /** Whether the response should be delivered as IR stream events rather than a single {@link IrResponse}. */
  stream: boolean;
  /** System-prompt content blocks, or null when none is set. */
  system?: Block[] | null;
  /** Sampling temperature, or null when unspecified. */
  temperature?: number | null;
  /** Extended-thinking configuration, or null when unspecified. */
  thinking?: IrThinking | null;
  /** Constrains how the model may use `tools`, or null when unspecified. */
  toolChoice?: IrToolChoice | null;
  /** Tools the model may call, or null when none are offered. */
  tools?: IrTool[] | null;
  /** Top-k sampling limit, or null when unspecified. */
  topK?: number | null;
  /** Nucleus-sampling threshold, or null when unspecified. */
  topP?: number | null;
}

/**
 * Token accounting, shared by {@link IrResponse} and streaming `MessageStart`/
 * `MessageDelta`. `reasoningTokens`/`totalTokens` are null for vendors with no
 * such concept (some vendors fold reasoning into `outputTokens` and report no derived total);
 * others populate both from their own separate reasoning/total-token usage fields.
 */
export interface IrUsage {
  /** Input tokens written to a prompt cache, or null when the vendor does not report it. */
  cacheCreationInputTokens?: number | null;
  /** Input tokens served from a prompt cache, or null when the vendor does not report it. */
  cacheReadInputTokens?: number | null;
  /** Tokens in the request's input, or null when the vendor does not report it. */
  inputTokens?: number | null;
  /** Tokens the model generated, or null when the vendor does not report it. */
  outputTokens?: number | null;
  /** Tokens spent on reasoning, or null for vendors with no such concept. */
  reasoningTokens?: number | null;
  /** The vendor's own reported token total, or null for vendors that report none. */
  totalTokens?: number | null;
}

/**
 * What an {@link IrHandler} is handed alongside one request.
 *
 * @remarks
 * `store` is the host's injected store, and a handler must serve from it rather than
 * assembling its own, so that every handler in a host shares one view of the same state. It is
 * `null` only on a store-less host, which is the one case where a handler may fall back to
 * something of its own.
 */
export interface HandlerCtx {
  /** The app home this handler reads its own configuration and state from. */
  configDir: string;
  /**
   * The `IrHandler.id()` this call resolved to, which a plugin backing several lanes off one
   * driver reads to pick between them.
   */
  handlerId: string;
  /** Where this handler's diagnostics go. */
  log: Logger;
  /** The model the request names, which is what a routing chain matched on. */
  model: string;
  /** The host's injected store, or `null` on a store-less host. */
  store: Store | null;
}

/**
 * `auto|any|none|{name`}. `type` holds one of `AUTO`/`ANY`/
 * `NONE`/`TOOL`; `name` is set only for `TOOL`.
 *
 *
 * `extensions` carries vendor-specific fields with no neutral home (e.g. a vendor's own
 * `disable_parallel_tool_use`).
 */
export interface IrToolChoice {
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** The tool the model must call, set only when `type` is `TOOL`. */
  name?: string | null;
  /** One of the `Type` constants controlling whether and how the model must use tools. */
  type: string;
}

/**
 * {@link IrResponse.stopReason} / streaming `MessageDelta.stopReason` constants.
 *
 * @remarks
 * Open, because a vendor may report a reason this set has no reading of yet and a
 * translator carries it through rather than losing it.
 */
export type IrStopReason = "end_turn" | "max_tokens" | "tool_use" | "stop_sequence" | "pause_turn" | "refusal" | "error" | (string & {});

/** A model-issued tool call. `input` is a parsed JSON tree (Map/List/String/Number/Boolean/null). */
export interface ToolUseBlock {
  /** Vendor-specific cache-control hint carried verbatim, or null when the vendor sets none. */
  cacheControl?: string | null;
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** An identifier for this call, referenced by the matching {@link ToolResultBlock.toolUseId}. */
  id: string;
  /** The call's arguments, as a parsed JSON tree. */
  input: unknown;
  /** The JSON discriminator identifying the concrete subclass; one of the {@link BlockKind} constants. */
  kind: "tool_use";
  /** The {@link IrTool.name} being called. */
  name: string;
}

/** An incremental raw-JSON-text chunk of a tool call's input, for the block at `index`. */
export interface ToolInputDeltaEvent {
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "tool_input_delta";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** Position of the content block this chunk belongs to. */
  index: number;
  /** The incremental raw-JSON-text chunk of the tool call's input. */
  partialJson?: string | null;
}

/** An incremental text chunk for the content block at `index`. */
export interface TextDeltaEvent {
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "text_delta";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** Position of the content block this chunk belongs to. */
  index: number;
  /** The incremental text chunk. */
  text?: string | null;
}

/** An incremental thinking-text chunk for the content block at `index`. */
export interface ThinkingDeltaEvent {
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "thinking_delta";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** Position of the content block this chunk belongs to. */
  index: number;
  /** The incremental thinking-text chunk. */
  text?: string | null;
}

/** Carries the final `stopReason` and/or updated usage, ahead of {@link MessageStopEvent}. */
export interface MessageDeltaEvent {
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "message_delta";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** The response's final stop reason, or null when not yet known. */
  stopReason?: IrStopReason | null;
  /** Updated token usage, or null when not yet known. */
  usage?: IrUsage | null;
}

/** Closes the content block at `index`. */
export interface ContentBlockStopEvent {
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "content_block_stop";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** Position of the content block that is closing. */
  index: number;
}

/** Closes the streamed response. Carries no fields beyond the discriminator. */
export interface MessageStopEvent {
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "message_stop";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
}

/** Delivers the vendor's opaque thinking-verification signature for the block at `index`. */
export interface ThinkingSignatureEvent {
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "thinking_signature";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** Position of the content block this signature verifies. */
  index: number;
  /** The vendor's opaque signature value. */
  signature?: string | null;
}

/** Extended-thinking request config: `{enabled, budgetTokens?`}. */
export interface IrThinking {
  /** Token budget allotted to thinking, or null when unspecified. */
  budgetTokens?: number | null;
  /** Whether extended thinking is requested for this call. */
  enabled: boolean;
}

/** Image content block: either inline base64 `data` or a `url`, not both. */
export interface ImageBlock {
  /** Vendor-specific cache-control hint carried verbatim, or null when the vendor sets none. */
  cacheControl?: string | null;
  /** Base64-encoded image bytes, or null when `url` is set instead. */
  data?: string | null;
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** The JSON discriminator identifying the concrete subclass; one of the {@link BlockKind} constants. */
  kind: "image";
  /** The image's MIME type, e.g. `image/png`. */
  mediaType: string;
  /** A URL the image can be fetched from, or null when `data` is set instead. */
  url?: string | null;
}

/** JSON discriminator values for {@link Block} subclasses. */
export type BlockKind = "text" | "image" | "tool_use" | "tool_result" | "thinking" | "unknown";

/** One turn in {@link IrRequest.messages}. `role` is `user|assistant|tool`. */
export interface IrMessage {
  /** The message's content blocks, in order. */
  content: Block[];
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** Who sent the message: `user`, `assistant`, or `tool`. */
  role: string;
}

/** Opens a streamed response: the message id/model/role and (if known up front) partial usage. */
export interface MessageStartEvent {
  /** The JSON discriminator, one of the `IrEventType` constants. */
  event: "message_start";
  /** Vendor-specific passthrough with no neutral home, or null when none applies. */
  extensions?: Record<string, unknown> | null;
  /** The response message's id. */
  id?: string | null;
  /** The model that produced the response. */
  model?: string | null;
  /** The response message's role, typically `assistant`. */
  role?: string | null;
  /** Token usage known at stream start, or null when the vendor reports it only later. */
  usage?: IrUsage | null;
}

/** Plain text content block. */
export interface TextBlock {
  /** Vendor-specific cache-control hint carried verbatim, or null when the vendor sets none. */
  cacheControl?: string | null;
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** The JSON discriminator identifying the concrete subclass; one of the {@link BlockKind} constants. */
  kind: "text";
  /** The block's text. */
  text: string;
}

/** The caller's result for a prior {@link ToolUseBlock}, referenced by `toolUseId`. */
export interface ToolResultBlock {
  /** Vendor-specific cache-control hint carried verbatim, or null when the vendor sets none. */
  cacheControl?: string | null;
  /** The tool's result, as content blocks. */
  content: Block[];
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** Whether the tool call failed, or null when the caller does not report success or failure. */
  isError?: boolean | null;
  /** The JSON discriminator identifying the concrete subclass; one of the {@link BlockKind} constants. */
  kind: "tool_result";
  /** The {@link ToolUseBlock.id} this result answers. */
  toolUseId: string;
}

/** The canonical, vendor-neutral non-streaming response. */
export interface IrResponse {
  /** The response's content blocks, in order. */
  content: Block[];
  /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
  extensions?: Record<string, unknown> | null;
  /** The response identifier assigned by the upstream vendor. */
  id: string;
  /** The model that produced the response, as reported by the upstream vendor. */
  model: string;
  /** Why generation stopped; one of the {@link IrStopReason} constants. */
  stopReason: IrStopReason;
  /** Token accounting for the request/response pair, or null when the vendor reports none. */
  usage?: IrUsage | null;
}

/** {@link ContentBlockStartEvent.blockKind} values. */
export type ContentBlockKind = "text" | "tool_use" | "thinking";

