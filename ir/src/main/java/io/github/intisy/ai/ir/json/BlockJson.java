package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.Block;
import io.github.intisy.ai.ir.BlockKind;
import io.github.intisy.ai.ir.ImageBlock;
import io.github.intisy.ai.ir.TextBlock;
import io.github.intisy.ai.ir.ThinkingBlock;
import io.github.intisy.ai.ir.ToolResultBlock;
import io.github.intisy.ai.ir.ToolUseBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link Block} hierarchy <-> {@code Map}/{@code List} tree, dispatching on {@link BlockKind}. */
final class BlockJson {
    private BlockJson() {
    }

    static Map<String, Object> toMap(Block b) {
        if (b == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kind", b.kind);
        if (b.cacheControl != null) m.put("cacheControl", b.cacheControl);
        if (b instanceof TextBlock) {
            m.put("text", ((TextBlock) b).text);
        } else if (b instanceof ImageBlock) {
            ImageBlock img = (ImageBlock) b;
            m.put("mediaType", img.mediaType);
            if (img.data != null) m.put("data", img.data);
            if (img.url != null) m.put("url", img.url);
        } else if (b instanceof ToolUseBlock) {
            ToolUseBlock t = (ToolUseBlock) b;
            m.put("id", t.id);
            m.put("name", t.name);
            m.put("input", t.input);
        } else if (b instanceof ToolResultBlock) {
            ToolResultBlock r = (ToolResultBlock) b;
            m.put("toolUseId", r.toolUseId);
            m.put("content", toMapList(r.content));
            if (r.isError != null) m.put("isError", r.isError);
        } else if (b instanceof ThinkingBlock) {
            ThinkingBlock t = (ThinkingBlock) b;
            m.put("text", t.text);
            if (t.signature != null) m.put("signature", t.signature);
        } else {
            throw new IllegalArgumentException("unsupported Block type: " + b.getClass());
        }
        if (b.extensions != null) m.put("extensions", b.extensions);
        return m;
    }

    static List<Object> toMapList(List<Block> blocks) {
        if (blocks == null) return null;
        List<Object> out = new ArrayList<>();
        for (Block b : blocks) out.add(toMap(b));
        return out;
    }

    @SuppressWarnings("unchecked")
    static Block fromMap(Object o) {
        Map<String, Object> m = JsonUtil.asMap(o);
        if (m == null) return null;
        String kind = JsonUtil.asString(m.get("kind"));
        Block b;
        if (BlockKind.TEXT.equals(kind)) {
            TextBlock t = new TextBlock();
            t.text = JsonUtil.asString(m.get("text"));
            b = t;
        } else if (BlockKind.IMAGE.equals(kind)) {
            ImageBlock img = new ImageBlock();
            img.mediaType = JsonUtil.asString(m.get("mediaType"));
            img.data = JsonUtil.asString(m.get("data"));
            img.url = JsonUtil.asString(m.get("url"));
            b = img;
        } else if (BlockKind.TOOL_USE.equals(kind)) {
            ToolUseBlock t = new ToolUseBlock();
            t.id = JsonUtil.asString(m.get("id"));
            t.name = JsonUtil.asString(m.get("name"));
            t.input = m.get("input");
            b = t;
        } else if (BlockKind.TOOL_RESULT.equals(kind)) {
            ToolResultBlock r = new ToolResultBlock();
            r.toolUseId = JsonUtil.asString(m.get("toolUseId"));
            r.content = fromMapList(m.get("content"));
            r.isError = JsonUtil.asBoolean(m.get("isError"));
            b = r;
        } else if (BlockKind.THINKING.equals(kind)) {
            ThinkingBlock t = new ThinkingBlock();
            t.text = JsonUtil.asString(m.get("text"));
            t.signature = JsonUtil.asString(m.get("signature"));
            b = t;
        } else {
            throw new IllegalArgumentException("unsupported block kind: " + kind);
        }
        b.cacheControl = JsonUtil.asString(m.get("cacheControl"));
        b.extensions = (Map<String, Object>) m.get("extensions");
        return b;
    }

    static List<Block> fromMapList(Object o) {
        List<Object> list = JsonUtil.asList(o);
        if (list == null) return null;
        List<Block> out = new ArrayList<>();
        for (Object item : list) out.add(fromMap(item));
        return out;
    }
}
