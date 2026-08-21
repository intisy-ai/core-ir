// Expected to FAIL type-checking: handleIr's second parameter is a ProviderCallContext, so an
// implementation declaring it as a string is not a ProviderCapability.
import type { ProviderCapability } from "../../src/generated/ir-contracts.js";

export const wrong: ProviderCapability = {
  id: "demo",
  handleIr: async (request, context: string) => {
    void request;
    void context;
    throw new Error("unreachable");
  },
};
