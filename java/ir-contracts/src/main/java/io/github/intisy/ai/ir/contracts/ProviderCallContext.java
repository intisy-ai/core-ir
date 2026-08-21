package io.github.intisy.ai.ir.contracts;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.function.Consumer;

/**
 * What a provider is handed alongside the request.
 *
 * @implNote {@code provider} is the resolved lane id, which a plugin backing several lanes off one
 * driver reads to pick between them. This is deliberately not the proxy engine's own handler
 * context, which additionally carries a host-injected store and serves the wire-level handler.
 */
@TsInterface(data = true)
public interface ProviderCallContext {
    String configDir();

    Consumer<String> log();

    String model();

    String provider();
}
