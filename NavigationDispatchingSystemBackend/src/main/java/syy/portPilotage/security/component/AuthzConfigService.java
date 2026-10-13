package syy.portPilotage.security.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import syy.portPilotage.entity.SystemParameters;
import syy.portPilotage.mapper.SystemParametersMapper;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class AuthzConfigService {

    private final SystemParametersMapper systemParametersMapper;
    private final ObjectMapper objectMapper;

    // 简单TTL缓存，避免每个请求都查参数表
    private final AtomicLong lastLoadAtMs = new AtomicLong(0);
    private volatile Map<String, List<String>> cachedRoleAliases = new HashMap<>();
    private volatile Map<String, List<String>> cachedPerms = new HashMap<>();
    private static final long TTL_MS = Duration.ofSeconds(60).toMillis();

    public AuthzConfigService(SystemParametersMapper systemParametersMapper) {
        this.systemParametersMapper = systemParametersMapper;
        this.objectMapper = new ObjectMapper();
    }

    public List<String> getRoleAliases(String userType) {
        ensureLoaded();
        if (userType == null) return List.of();
        String key = userType.trim();
        if (key.isEmpty()) return List.of();
        List<String> v = cachedRoleAliases.get(key);
        if (v != null) return v;
        v = cachedRoleAliases.get(key.toUpperCase());
        if (v != null) return v;
        v = cachedRoleAliases.get(key.toLowerCase());
        return v != null ? v : List.of();
    }

    public List<String> getPermissions(String userType) {
        ensureLoaded();
        if (userType == null) return List.of();
        String key = userType.trim();
        if (key.isEmpty()) return List.of();
        List<String> v = cachedPerms.get(key);
        if (v != null) return v;
        v = cachedPerms.get(key.toUpperCase());
        if (v != null) return v;
        v = cachedPerms.get(key.toLowerCase());
        return v != null ? v : List.of();
    }

        public void invalidate() {
        lastLoadAtMs.set(0);
    }

    private void ensureLoaded() {
        long now = System.currentTimeMillis();
        long last = lastLoadAtMs.get();
        if (now - last < TTL_MS) return;
        // 双重检查 + 简单锁（用 synchronized）
        synchronized (this) {
            last = lastLoadAtMs.get();
            if (now - last < TTL_MS) return;
            try {
                List<SystemParameters> list = systemParametersMapper.selectByGroup("AUTH");
                Map<String, String> map = new HashMap<>();
                for (SystemParameters p : list) {
                    if (p == null || p.getParamKey() == null) continue;
                    map.put(p.getParamKey(), p.getParamValue());
                }
                cachedRoleAliases = parseJsonMapOfStringList(map.get("USER_TYPE_ROLE_ALIASES"));
                cachedPerms = parseJsonMapOfStringList(map.get("USER_TYPE_PERMISSIONS"));
            } catch (Exception e) {
                log.warn("加载 AUTH 参数失败，继续使用旧缓存: {}", e.getMessage());
            } finally {
                lastLoadAtMs.set(now);
            }
        }
    }

    private Map<String, List<String>> parseJsonMapOfStringList(String json) {
        if (json == null || json.trim().isEmpty()) return new HashMap<>();
        try {
            Map<String, List<String>> parsed = objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {});
            Map<String, List<String>> result = new HashMap<>();
            for (Map.Entry<String, List<String>> e : parsed.entrySet()) {
                if (e.getKey() == null) continue;
                List<String> v = e.getValue() == null ? List.of() : e.getValue().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                String k = e.getKey().trim();
                if (k.isEmpty()) continue;
                // 统一把 key 的大小写兼容掉：同一份权限同时写入原 key、upper、lower，避免 user_type 与配置 key 不一致导致“权限不生效”
                mergeList(result, k, v);
                mergeList(result, k.toUpperCase(), v);
                mergeList(result, k.toLowerCase(), v);
            }
            return result;
        } catch (Exception e) {
            log.warn("解析 AUTH JSON 参数失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private void mergeList(Map<String, List<String>> map, String key, List<String> add) {
        if (key == null || key.isEmpty()) return;
        if (add == null || add.isEmpty()) {
            map.putIfAbsent(key, List.of());
            return;
        }
        List<String> existing = map.get(key);
        if (existing == null || existing.isEmpty()) {
            map.put(key, add);
            return;
        }
        LinkedHashSet<String> set = new LinkedHashSet<>(existing);
        set.addAll(add);
        map.put(key, new ArrayList<>(set));
    }
}
