package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.IrResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@link IrResponse} <-> {@code Map} tree. */
final class IrResponseJson {
    private IrResponseJson() {
    }

    static Map<String, Object> toMap(IrResponse r) {
        if (r == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.id);
        m.put("model", r.model);
        m.put("content", BlockJson.toMapList(r.content));
        m.put("stopReason", r.stopReason);
        if (r.usage != null) m.put("usage", CommonJson.toMap(r.usage));
        if (r.extensions != null) m.put("extensions", r.extensions);
        return m;
    }

    @SuppressWarnings("unchecked")
    static IrResponse fromMap(Map<String, Object> m) {
        if (m == null) return null;
        IrResponse r = new IrResponse();
        r.id = JsonUtil.asString(m.get("id"));
        r.model = JsonUtil.asString(m.get("model"));
        r.content = BlockJson.fromMapList(m.get("content"));
        r.stopReason = JsonUtil.asString(m.get("stopReason"));
        r.usage = CommonJson.usageFromMap(m.get("usage"));
        r.extensions = (Map<String, Object>) m.get("extensions");
        return r;
    }
}
