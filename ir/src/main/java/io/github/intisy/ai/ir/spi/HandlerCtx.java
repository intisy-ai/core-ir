package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.api.seam.Logger;
import io.github.intisy.ai.api.seam.Store;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;

/**
 * What an {@link IrHandler} is handed alongside one request.
 *
 * @implNote {@code store} is the host's injected store, and a handler must serve from it rather than
 * assembling its own, so that every handler in a host shares one view of the same state. It is
 * {@code null} only on a store-less host, which is the one case where a handler may fall back to
 * something of its own.
 */
@TsInterface(data = true)
public class HandlerCtx {
    /** The app home this handler reads its own configuration and state from. */
    public String configDir;

    /** The host's injected store, or {@code null} on a store-less host. */
    @TsNullable
    public Store store;

    /** Where this handler's diagnostics go. */
    public Logger log;

    /** The model the request names, which is what a routing chain matched on. */
    public String model;

    /**
     * The {@code IrHandler.id()} this call resolved to, which a plugin backing several lanes off one
     * driver reads to pick between them.
     */
    public String handlerId;

    /** Creates a context with no fields set yet. */
    public HandlerCtx() {
    }

    /**
     * Creates a context for a store-less host.
     *
     * @param configDir the app home this handler reads its own configuration and state from.
     * @param log where this handler's diagnostics go.
     * @param model the model the request names.
     */
    public HandlerCtx(String configDir, Logger log, String model) {
        this.configDir = configDir;
        this.log = log;
        this.model = model;
    }

    /**
     * @param configDir the app home this handler reads its own configuration and state from.
     * @param store the host's injected store.
     * @param log where this handler's diagnostics go.
     * @param model the model the request names.
     */
    public HandlerCtx(String configDir, Store store, Logger log, String model) {
        this.configDir = configDir;
        this.store = store;
        this.log = log;
        this.model = model;
    }
}
