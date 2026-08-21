// Generated from Java sources. Do not edit.

import type { IrEventStream, IrRequest, IrResponse } from "../types.js";

export interface ProviderCallContext {
  configDir: string;
  log: ((value: string) => void);
  model: string;
  provider: string;
}

export interface ProviderCapability {
  handleIr(request: IrRequest, context: ProviderCallContext): Promise<IrResponse | IrEventStream>;
  readonly id: string;
  providers?(): ProviderDescriptor[] | Promise<ProviderDescriptor[]>;
}

export interface ProviderDescriptor {
  accountPool?: string;
  hasOAuth?: boolean;
  id: string;
  label: string;
  models?: Record<string, unknown>;
  translator?: string;
}

