package syy.portPilotage.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class HttpUtil {
    
    private static final int TIMEOUT = 30000;
    
    public static String get(String url) {
        return get(url, null);
    }
    
    public static String get(String url, Map<String, Object> params) {
        try (HttpResponse response = HttpRequest.get(url)
                .form(params)
                .timeout(TIMEOUT)
                .execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("HTTP GET请求失败: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP请求失败", e);
        }
    }
    
    public static String post(String url, Map<String, Object> params) {
        try (HttpResponse response = HttpRequest.post(url)
                .form(params)
                .timeout(TIMEOUT)
                .execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("HTTP POST请求失败: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP请求失败", e);
        }
    }
    
    public static String postJson(String url, String json) {
        try (HttpResponse response = HttpRequest.post(url)
                .body(json)
                .contentType("application/json")
                .timeout(TIMEOUT)
                .execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("HTTP POST JSON请求失败: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP请求失败", e);
        }
    }
}
