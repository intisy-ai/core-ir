import type { CapabilityType } from "../../api/generated/api.js";
import type { ProviderCapability, ProviderDescriptor } from "../../src/generated/ir-contracts.js";
import { PROVIDER } from "../../src/generated/ir-contracts.keys.js";

const lane: ProviderDescriptor = { id: "demo", label: "Demo", hasOAuth: false };

const provider: ProviderCapability = {
  id: "demo",
  handleIr: async (request, context) => {
    context.log(`serving ${context.model} on ${context.provider} from ${context.configDir}`);
    return { id: request.model, model: request.model, role: "assistant", content: [] } as never;
  },
  providers: () => [lane],
};

const key: CapabilityType<ProviderCapability> = PROVIDER;

export const checked: [ProviderCapability, CapabilityType<ProviderCapability>] = [provider, key];
