package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;
import io.github.intisy.ai.tsemit.TsUnion;

/**
 * Answers one id's requests in canonical IR, never in any app's or vendor's wire format.
 *
 * @implNote Declares nothing {@code ServiceLoader}-specific and nothing JVM-only, so one interface
 * serves both discovery routes: a JVM host loads implementations through
 * {@code ServiceLoader.load(IrHandler.class)}, while a transpiled host has no classpath to scan and
 * instead constructs its handler directly and registers it under {@link #id()}.
 */
@TsInterface
public interface IrHandler {
    /** The id a routing chain names to reach this handler. */
    @TsProperty(readOnly = true)
    String id();

    /**
     * Serves an already-decoded request.
     *
     * @throws HandleIrException for a non-2xx upstream outcome, so the caller can rebuild the
     * response it describes. Any other throw is an unexpected failure.
     */
    @TsUnion(value = {"IrResponse", "IrEventStream"}, async = true)
    IrResponse handleIr(IrRequest request, HandlerCtx ctx) throws Exception;
}
