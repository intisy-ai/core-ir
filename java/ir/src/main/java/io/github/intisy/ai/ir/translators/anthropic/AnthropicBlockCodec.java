package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.Block;
import io.github.intisy.ai.ir.ImageBlock;
import io.github.intisy.ai.ir.TextBlock;
import io.github.intisy.ai.ir.ThinkingBlock;
import io.github.intisy.ai.ir.ToolResultBlock;
import io.github.intisy.ai.ir.ToolUseBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Anthropic content-block {@code Map} tree <-> {@link Block} hierarchy. Anthropic maps near-1:1
 * onto the IR content-block model; anything with no neutral home (block-level
 * {@code cache_control} extras, unrecognized image sources, {@code redacted_thinking}'s opaque
 * {@code data}, and any field this codec does not otherwise know) round-trips through
 * {@link Block#extensions}, keyed with a {@code $} prefix when it is bookkeeping this codec
 * invented (never a real Anthropic wire key) so it is never re-emitted as a genuine field.
 */
final class AnthropicBlockCodec {
    private AnthropicBlockCodec() {
    }

    static final String EXT_CACHE_CONTROL_EXTRA = "$cacheControlExtra";
    static final String EXT_IMAGE_SOURCE_RAW = "$imageSourceRaw";
    static final String EXT_CONTENT_IS_STRING = "$contentIsString";
    static final String EXT_REDACTED = "$redacted";
    static final String EXT_REDACTED_DATA = "$redactedThinkingData";

    static void putExtension(Block block, String key, Object value) {
        if (block.extensions == null) block.extensions = new LinkedHashMap<>();
        block.extensions.put(key, value);
    }

    static List<Object> encodeBlockList(List<Block> blocks) {
        List<Object> out = new ArrayList<>();
        if (blocks == null) return out;
        for (Block b : blocks) out.add(encodeBlock(b));
        return out;
    }

    static List<Block> decodeBlockList(Object raw) {
        List<Object> list = AnthropicJsonUtil.asList(raw);
        if (list == null) return null;
        List<Block> out = new ArrayList<>();
        for (Object item : list) out.add(decodeBlock(AnthropicJsonUtil.asMap(item)));
        return out;
    }

    /** Wraps plain-string content ({@code system}/message/tool_result content) as a single block. */
    static List<Block> wrapStringAsBlocks(String text) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(new TextBlock(text));
        return blocks;
    }

    /** True when {@code blocks} is exactly the shape produced by {@link #wrapStringAsBlocks}. */
    static boolean isPlainWrappedText(List<Block> blocks) {
        if (blocks == null || blocks.size() != 1) return false;
        Block only = blocks.get(0);
        return only instanceof TextBlock && only.cacheControl == null && only.extensions == null;
    }

    @SuppressWarnings("unchecked")
    static Block decodeBlock(Map<String, Object> m) {
        if (m == null) return null;
        String type = AnthropicJsonUtil.asString(m.get("type"));
        Set<String> consumed = new HashSet<>(Arrays.asList("type", "cache_control"));
        Block block;
        if ("text".equals(type)) {
            TextBlock t = new TextBlock();
            t.text = AnthropicJsonUtil.asString(m.get("text"));
            consumed.add("text");
            block = t;
        } else if ("image".equals(type)) {
            block = decodeImageBlock(m);
            consumed.add("source");
        } else if ("tool_use".equals(type)) {
            ToolUseBlock t = new ToolUseBlock();
            t.id = AnthropicJsonUtil.asString(m.get("id"));
            t.name = AnthropicJsonUtil.asString(m.get("name"));
            t.input = m.get("input");
            consumed.addAll(Arrays.asList("id", "name", "input"));
            block = t;
        } else if ("tool_result".equals(type)) {
            block = decodeToolResultBlock(m);
            consumed.addAll(Arrays.asList("tool_use_id", "is_error", "content"));
        } else if ("thinking".equals(type)) {
            ThinkingBlock t = new ThinkingBlock();
            t.text = AnthropicJsonUtil.asString(m.get("thinking"));
            t.signature = AnthropicJsonUtil.asString(m.get("signature"));
            consumed.addAll(Arrays.asList("thinking", "signature"));
            block = t;
        } else if ("redacted_thinking".equals(type)) {
            ThinkingBlock t = new ThinkingBlock();
            putExtension(t, EXT_REDACTED, Boolean.TRUE);
            putExtension(t, EXT_REDACTED_DATA, m.get("data"));
            consumed.add("data");
            block = t;
        } else {
            throw new IllegalArgumentException("unsupported Anthropic content block type: " + type);
        }
        decodeCacheControl(m, block);
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (!consumed.contains(e.getKey())) {
                putExtension(block, e.getKey(), e.getValue());
            }
        }
        return block;
    }

    private static Block decodeImageBlock(Map<String, Object> m) {
        ImageBlock img = new ImageBlock();
        Map<String, Object> source = AnthropicJsonUtil.asMap(m.get("source"));
        if (source != null) {
            String sourceType = AnthropicJsonUtil.asString(source.get("type"));
            if ("base64".equals(sourceType)) {
                img.mediaType = AnthropicJsonUtil.asString(source.get("media_type"));
                img.data = AnthropicJsonUtil.asString(source.get("data"));
            } else if ("url".equals(sourceType)) {
                img.url = AnthropicJsonUtil.asString(source.get("url"));
            } else {
                // e.g. Files-API "file" source -- no neutral field for it, keep it verbatim.
                putExtension(img, EXT_IMAGE_SOURCE_RAW, source);
            }
        }
        return img;
    }

    private static Block decodeToolResultBlock(Map<String, Object> m) {
        ToolResultBlock r = new ToolResultBlock();
        r.toolUseId = AnthropicJsonUtil.asString(m.get("tool_use_id"));
        r.isError = AnthropicJsonUtil.asBoolean(m.get("is_error"));
        Object contentRaw = m.get("content");
        if (contentRaw instanceof String) {
            r.content = wrapStringAsBlocks((String) contentRaw);
            putExtension(r, EXT_CONTENT_IS_STRING, Boolean.TRUE);
        } else {
            r.content = decodeBlockList(contentRaw);
        }
        return r;
    }

    private static void decodeCacheControl(Map<String, Object> m, Block block) {
        Map<String, Object> cc = AnthropicJsonUtil.asMap(m.get("cache_control"));
        if (cc == null) return;
        block.cacheControl = AnthropicJsonUtil.asString(cc.get("type"));
        Map<String, Object> extra = new LinkedHashMap<>(cc);
        extra.remove("type");
        if (!extra.isEmpty()) {
            putExtension(block, EXT_CACHE_CONTROL_EXTRA, extra);
        }
    }

    static Map<String, Object> encodeBlock(Block block) {
        if (block == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        if (block instanceof TextBlock) {
            m.put("type", "text");
            m.put("text", ((TextBlock) block).text);
        } else if (block instanceof ImageBlock) {
            encodeImageBlock((ImageBlock) block, m);
        } else if (block instanceof ToolUseBlock) {
            ToolUseBlock t = (ToolUseBlock) block;
            m.put("type", "tool_use");
            m.put("id", t.id);
            m.put("name", t.name);
            m.put("input", t.input);
        } else if (block instanceof ToolResultBlock) {
            encodeToolResultBlock((ToolResultBlock) block, m);
        } else if (block instanceof ThinkingBlock) {
            encodeThinkingBlock((ThinkingBlock) block, m);
        } else {
            throw new IllegalArgumentException("unsupported Block type: " + block.getClass());
        }
        encodeCacheControl(block, m);
        encodeLeftoverExtensions(block, m);
        return m;
    }

    private static void encodeImageBlock(ImageBlock img, Map<String, Object> m) {
        m.put("type", "image");
        Map<String, Object> rawSource = img.extensions == null ? null
                : AnthropicJsonUtil.asMap(img.extensions.get(EXT_IMAGE_SOURCE_RAW));
        if (rawSource != null) {
            m.put("source", rawSource);
        } else if (img.data != null) {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("type", "base64");
            source.put("media_type", img.mediaType);
            source.put("data", img.data);
            m.put("source", source);
        } else if (img.url != null) {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("type", "url");
            source.put("url", img.url);
            m.put("source", source);
        }
    }

    private static void encodeToolResultBlock(ToolResultBlock r, Map<String, Object> m) {
        m.put("type", "tool_result");
        m.put("tool_use_id", r.toolUseId);
        boolean wasString = r.extensions != null && Boolean.TRUE.equals(r.extensions.get(EXT_CONTENT_IS_STRING));
        if (wasString && isPlainWrappedText(r.content)) {
            m.put("content", ((TextBlock) r.content.get(0)).text);
        } else {
            m.put("content", encodeBlockList(r.content));
        }
        if (r.isError != null) m.put("is_error", r.isError);
    }

    private static void encodeThinkingBlock(ThinkingBlock t, Map<String, Object> m) {
        boolean redacted = t.extensions != null && Boolean.TRUE.equals(t.extensions.get(EXT_REDACTED));
        if (redacted) {
            m.put("type", "redacted_thinking");
            m.put("data", t.extensions.get(EXT_REDACTED_DATA));
        } else {
            m.put("type", "thinking");
            m.put("thinking", t.text);
            if (t.signature != null) m.put("signature", t.signature);
        }
    }

    private static void encodeCacheControl(Block block, Map<String, Object> m) {
        if (block.cacheControl == null) return;
        Map<String, Object> cc = new LinkedHashMap<>();
        cc.put("type", block.cacheControl);
        Map<String, Object> extra = block.extensions == null ? null
                : AnthropicJsonUtil.asMap(block.extensions.get(EXT_CACHE_CONTROL_EXTRA));
        if (extra != null) cc.putAll(extra);
        m.put("cache_control", cc);
    }

    private static void encodeLeftoverExtensions(Block block, Map<String, Object> m) {
        if (block.extensions == null) return;
        for (Map.Entry<String, Object> e : block.extensions.entrySet()) {
            if (!e.getKey().startsWith("$")) {
                m.put(e.getKey(), e.getValue());
            }
        }
    }
}
