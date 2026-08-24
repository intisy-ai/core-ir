// Expected to FAIL type-checking: handleIr's second parameter is a HandlerCtx, so an implementation
// declaring it as a string is not an IrHandler.
import type { IrHandler } from "../../src/generated/ir-spi.js";

export const wrong: IrHandler = {
  id: "demo",
  handleIr: async (request, ctx: string) => {
    void request;
    void ctx;
    throw new Error("unreachable");
  },
};
