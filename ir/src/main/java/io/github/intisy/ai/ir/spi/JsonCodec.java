package io.github.intisy.ai.ir.spi;

/**
 * JSON boundary SPI. The parsed shape is a plain {@code Object} tree built from
 * {@code java.util.Map}/{@code java.util.List}/{@code String}/{@code Number}/{@code Boolean}/
 * {@code null}, matching what gson and JS {@code JSON.parse} both naturally produce.
 */
public interface JsonCodec {
    /**
     * @param json the JSON text to parse.
     * @return the parsed value, one of {@code Map}, {@code List}, {@code String}, {@code Number},
     * {@code Boolean}, or null for JSON {@code null}.
     */
    Object parse(String json);

    /**
     * @param value the value to serialize, one of {@code Map}, {@code List}, {@code String},
     * {@code Number}, {@code Boolean}, or null.
     * @return the value's JSON text.
     */
    String stringify(Object value);
}
