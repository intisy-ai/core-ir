package io.github.intisy.ai.ir.spi;

import java.util.Map;

/**
 * The typed transport error an {@link IrHandler} throws for a non-2xx upstream outcome.
 *
 * @implNote It carries the status, headers and body so the caller can rebuild an equivalent response
 * and put it through whatever rate-limit and fallback handling it applies to any other response,
 * instead of collapsing every throw into one opaque failure. IR models a message, never an HTTP
 * envelope, which is why the envelope travels on the exception rather than in the response type.
 */
public class HandleIrException extends Exception {
    public final int status;
    public final Map<String, String> headers;
    public final String body;
    /**
     * A reset hint in milliseconds for a caller that wants to surface one, or {@code null} when the
     * upstream gave none. Left as a number so the thrower needs to know no header name.
     */
    public final Long retryAfterMs;

    public HandleIrException(int status, Map<String, String> headers, String body, Long retryAfterMs) {
        super("handleIr transport error: " + status);
        this.status = status;
        this.headers = headers;
        this.body = body;
        this.retryAfterMs = retryAfterMs;
    }

    public HandleIrException(int status, Map<String, String> headers, String body) {
        this(status, headers, body, null);
    }
}
