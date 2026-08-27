// Generated from Java sources. Do not edit.

/**
 * Wire JSON to an IR request and back, proving the request helper crosses TeaVM.
 *
 * @param wireJson - the request's JSON text.
 * @returns the same request, re-serialized.
 */
export declare function irRequestRoundTrip(wireJson: string): string;
/**
 * Wire JSON to an IR response and back.
 *
 * @param wireJson - the response's JSON text.
 * @returns the same response, re-serialized.
 */
export declare function irResponseRoundTrip(wireJson: string): string;
/**
 * Wire JSON to an IR stream event and back.
 *
 * @param wireJson - the stream event's JSON text.
 * @returns the same event, re-serialized.
 */
export declare function irStreamEventRoundTrip(wireJson: string): string;
/**
 * Parse and stringify with no IR type involved, proving the JSON codec crosses TeaVM.
 *
 * @param json - the JSON text to round-trip.
 * @returns the same value, re-serialized.
 */
export declare function jsonRoundTrip(json: string): string;

