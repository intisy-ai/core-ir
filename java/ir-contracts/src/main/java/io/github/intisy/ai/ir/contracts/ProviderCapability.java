package io.github.intisy.ai.ir.contracts;

import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsMaybeAsync;
import io.github.intisy.ai.tsemit.TsOptional;
import io.github.intisy.ai.tsemit.TsProperty;
import io.github.intisy.ai.tsemit.TsUnion;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Talks to one upstream vendor, in canonical IR only.
 *
 * @implNote A provider never sees an app's wire format: it translates IR into its own upstream vendor
 * format, calls upstream, and decodes the reply back into IR. On a non-2xx upstream outcome it THROWS
 * the typed handler error rather than returning it as data, so the front-door can rebuild the
 * response and rate-limit fallback keeps working.
 */
@TsInterface
public interface ProviderCapability {
    /** The provider id a routing chain names. */
    @TsProperty(readOnly = true)
    String id();

    @TsUnion({"IrResponse", "IrEventStream"})
    CompletionStage<IrResponse> handleIr(IrRequest request, ProviderCallContext context);

    /**
     * Every lane this plugin serves, when it serves more than the one {@code id} names.
     *
     * @implNote Optional because most providers are one lane, so a host that does not call it sees
     * exactly the behaviour it saw before this method existed.
     */
    @TsOptional
    @TsMaybeAsync
    List<ProviderDescriptor> providers();
}
