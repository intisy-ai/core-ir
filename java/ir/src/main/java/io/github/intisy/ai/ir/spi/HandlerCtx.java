package io.github.intisy.ai.ir.spi;

import io.github.intisy.ai.api.seam.Logger;
import io.github.intisy.ai.api.seam.Store;

/**
 * What an {@link IrHandler} is handed alongside one request.
 *
 * @implNote {@code store} is the host's injected store, and a handler must serve from it rather than
 * assembling its own, so that every handler in a host shares one view of the same state. It is
 * {@code null} only on a store-less host, which is the one case where a handler may fall back to
 * something of its own.
 */
public class HandlerCtx {
    public String configDir;
    public Store store;
    public Logger log;
    public String model;

    public HandlerCtx() {
    }

    public HandlerCtx(String configDir, Logger log, String model) {
        this.configDir = configDir;
        this.log = log;
        this.model = model;
    }

    public HandlerCtx(String configDir, Store store, Logger log, String model) {
        this.configDir = configDir;
        this.store = store;
        this.log = log;
        this.model = model;
    }
}
