package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.stream.IrEventSource;

/**
 * The streaming half of {@link IrHandler}, implemented by a handler that can serve a request whose
 * {@code IrRequest.stream} is set without buffering the whole response first.
 *
 * @implNote A sibling capability interface rather than a method on {@link IrHandler}, for the same
 * reason the app-wire path is a sibling {@code ProxyHandler}: adding a method would break every
 * existing implementation, and Java cannot express the {@code IrResponse | IrEventStream} union that
 * {@link IrHandler#handleIr} already emits to TypeScript. So one TypeScript function maps to two Java
 * methods, and a caller selects by {@code instanceof} plus the request's own stream flag.
 */
public interface IrStreamHandler extends IrHandler {

    /**
     * Serves an already-decoded streaming request.
     *
     * @param request the request to serve.
     * @param ctx the per-call context this handler was invoked with.
     * @return the events as they are produced; never {@code null}.
     * @throws HandleIrException for a non-2xx upstream outcome, so the caller can rebuild the
     * response it describes. Thrown before the first event, a failure is still retryable by the
     * caller; thrown from the returned source after an event has been delivered, it is not.
     */
    IrEventSource handleIrStream(IrRequest request, HandlerCtx ctx) throws Exception;
}
