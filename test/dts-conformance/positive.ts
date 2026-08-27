import type { HandlerCtx, IrHandler } from "../../src/generated/ir.js";

const handler: IrHandler = {
  id: "demo",
  handleIr: async (request, ctx: HandlerCtx) => {
    ctx.log.info(`serving ${ctx.model} on ${ctx.handlerId} from ${ctx.configDir}`);
    ctx.store?.put("last-model.json", request.model);
    return { id: request.model, model: request.model, content: [], stopReason: "end_turn" };
  },
};

export const checked: IrHandler = handler;
