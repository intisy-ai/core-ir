import { guardDocumentation, guardGeneratedSurface, guardNoSuppressions } from "@intisy-ai/api/testing";

guardDocumentation({ dir: new URL("..", import.meta.url) });
guardNoSuppressions({ dir: new URL("..", import.meta.url) });
guardGeneratedSurface({ files: [new URL("../generated/ir.ts", import.meta.url)] });
