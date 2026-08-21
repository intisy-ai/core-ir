package io.github.intisy.ai.ir.contracts;

import io.github.intisy.ai.tsemit.TsConstant;

/**
 * The typed key this package mints.
 *
 * @implNote The Java field type is {@code Object} and its value {@code null} because the Java side
 * never reads a key: a Java host keys on the id string, and the typed key exists for the emitted
 * TypeScript.
 */
public final class IrContracts {

    @TsConstant(type = "CapabilityType<ProviderCapability>", id = "provider")
    public static final Object PROVIDER = null;

    private IrContracts() {
    }
}
