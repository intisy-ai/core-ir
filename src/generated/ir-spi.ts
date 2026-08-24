// Generated from Java sources. Do not edit.

import type { IrEventStream, IrRequest, IrResponse } from "../types.js";
import type { Logger, Store } from "@intisy-ai/api/contract";

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
   * @throws HandleIrException for a non-2xx upstream outcome, so the caller can rebuild the
   * response it describes. Any other throw is an unexpected failure.
   */
  handleIr(request: IrRequest, ctx: HandlerCtx): Promise<IrResponse | IrEventStream>;
  /** The id a routing chain names to reach this handler. */
  readonly id: string;
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

