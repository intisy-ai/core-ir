// Generated from Java sources. Do not edit.

/** Wire JSON to an IR request and back, proving the request helper crosses TeaVM. */
export declare function irRequestRoundTrip(wireJson: string): string;
/** Wire JSON to an IR response and back. */
export declare function irResponseRoundTrip(wireJson: string): string;
/** Wire JSON to an IR stream event and back. */
export declare function irStreamEventRoundTrip(wireJson: string): string;
/** Parse and stringify with no IR type involved, proving the JSON codec crosses TeaVM. */
export declare function jsonRoundTrip(json: string): string;

