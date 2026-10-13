package syy.portPilotage.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import syy.portPilotage.ai.domain.AIChatRequest;
import syy.portPilotage.ai.domain.AIChatResponse;
import syy.portPilotage.ai.domain.StreamChatChunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnExpression("'${api.ai.openai.apiKey:}'.trim().length() > 0")
public class AIClient {

    @Value("${api.ai.openai.apiKey}")
    private String apiKey;

    @Value("${api.ai.openai.anythingllmUrl}")
    private String anythingllmUrl;

    @Value("${api.ai.openai.workspaceId}")
    private String workspaceId;

    @Value("${api.ai.openai.mode}")
    private String mode;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public AIChatResponse chatWithWorkspace(AIChatRequest request) {
        try (InputStream stream = streamChatWithWorkspace(request)) {
            AIChatResponse chatResponse = handleStreamingResponse(stream);
            log.info("流式聊天成功，响应ID: {}", chatResponse.getId());
            return chatResponse;
        } catch (IOException e) {
            log.error("读取流式聊天响应失败: {}", e.getMessage(), e);
            throw new RuntimeException("流式聊天调用失败: " + e.getMessage(), e);
        }
    }

    public InputStream streamChatWithWorkspace(AIChatRequest request) {
        String message = request != null ? request.getMessage() : null;
        String mode = request != null ? request.getMode() : null;
        String sessionId = request != null ? request.getSessionId() : null;
        Boolean reset = request != null ? request.getReset() : null;
        List<AIChatResponse.Attachment> attachments = request != null ? request.getAttachments() : null;

        log.info("流式聊天: message={}, mode={}, sessionId={}", message, mode, sessionId);

        try {
            if (!"query".equals(mode) && !"chat".equals(mode)) {
                String fallbackMode = this.mode != null ? this.mode : "chat";
                log.warn("模式 {} 无效，使用默认模式: {}", mode, fallbackMode);
                mode = fallbackMode;
            }

            Map<String, Object> requestBodyMap = new java.util.LinkedHashMap<>();
            requestBodyMap.put("message", message);
            requestBodyMap.put("mode", mode);
            requestBodyMap.put("sessionId", sessionId != null ? sessionId : "default-session");
            requestBodyMap.put("reset", reset != null ? reset : Boolean.FALSE);
            if (attachments != null && !attachments.isEmpty()) {
                requestBodyMap.put("attachments", attachments);
            }

            String requestBody = objectMapper.writeValueAsString(requestBodyMap);
            log.debug("请求体: {}", requestBody);

            if (isBlank(anythingllmUrl) || isBlank(apiKey) || isBlank(workspaceId)) {
                log.error("缺少必要的环境变量配置");
                throw new RuntimeException("缺少必要的环境变量配置: anythingllmUrl, apiKey, 或 workspaceId");
            }

            String baseUrl = normalizeBaseUrl(anythingllmUrl);
            String chatEndpoint = String.format("%s/api/v1/workspace/%s/stream-chat", baseUrl, workspaceId);
            log.info("调用流式聊天端点: {}", chatEndpoint);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(chatEndpoint))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(300)) // 流式响应可能需要更长时间
                    .build();

            HttpResponse<InputStream> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                // 读取错误响应体
                String errorBody = readErrorResponse(response.body());
                log.error("流式聊天失败，状态码: {}, 响应: {}", response.statusCode(), errorBody);

                if (response.statusCode() == 403) {
                    throw new RuntimeException("API密钥无效或权限不足: " + errorBody);
                } else if (response.statusCode() == 400) {
                    throw new RuntimeException("请求参数错误: " + errorBody);
                } else {
                    throw new RuntimeException("流式聊天失败，状态码: " + response.statusCode() + ", 响应: " + errorBody);
                }
            }
        } catch (Exception e) {
            log.error("流式聊天调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("流式聊天调用失败: " + e.getMessage(), e);
        }
    }

        private AIChatResponse handleStreamingResponse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder fullResponse = new StringBuilder();
        List<AIChatResponse.Source> sources = new ArrayList<>();
        String id = null;
        String type = null;
        String error = null;
        boolean close = false;

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                log.debug("收到流式数据: {}", line);

                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6);

                    if (jsonData.trim().equals("[DONE]")) {
                        log.debug("收到流式结束信号");
                        break;
                    }

                    try {
                        // 使用ObjectMapper解析JSON
                        StreamChatChunk chunk = objectMapper.readValue(jsonData, StreamChatChunk.class);

                        String chunkId = chunk.getId() != null ? chunk.getId() : chunk.getUuid();
                        if (chunkId != null) {
                            id = chunkId;
                        }

                        if (chunk.getType() != null) {
                            type = chunk.getType();
                        }

                        if (chunk.getTextResponse() != null) {
                            fullResponse.append(chunk.getTextResponse());
                        }

                        String errorText = normalizeError(chunk.getError());
                        if (errorText != null) {
                            error = errorText;
                        }

                        if (chunk.getClose() != null) {
                            close = chunk.getClose();
                        }

                        // 只在最后一个chunk中收集sources
                        if (chunk.getSources() != null && !chunk.getSources().isEmpty()) {
                            sources = chunk.getSources();
                        }

                        // 如果收到结束标志，则退出循环
                        if (Boolean.TRUE.equals(close)) {
                            break;
                        }

                    } catch (Exception e) {
                        log.warn("解析流式响应数据时出错，数据: {}, 错误: {}", jsonData, e.getMessage());
                    }
                }
            }
        } finally {
            reader.close();
        }

        return AIChatResponse.builder()
                .id(id != null ? id : "chat-" + System.currentTimeMillis())
                .type(type != null ? type : "textResponse")
                .textResponse(fullResponse.toString())
                .sources(sources)
                .close(close)
                .error(error)
                .build();
    }

        private String readErrorResponse(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) return null;
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String normalizeError(Object error) {
        if (error == null) return null;
        if (error instanceof Boolean) {
            return (Boolean) error ? "true" : null;
        }
        if (error instanceof String) {
            String text = ((String) error).trim();
            return "null".equalsIgnoreCase(text) ? null : text;
        }
        return error.toString();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}