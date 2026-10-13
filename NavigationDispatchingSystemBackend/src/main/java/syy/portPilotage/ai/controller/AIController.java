package syy.portPilotage.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import syy.portPilotage.ai.domain.AIChatRequest;
import syy.portPilotage.ai.client.AIClient;
import syy.portPilotage.ai.domain.AIChatResponse;
import org.springframework.util.StringUtils;

import java.io.InputStream;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI服务", description = "AI相关功能接口")
@ConditionalOnBean(AIClient.class)
public class AIController {

    @Autowired
    private AIClient aiClient;

    @PostMapping("/chat")
    @Operation(summary = "与工作区聊天", description = "向AI工作区发送消息并获得响应")
    public AIChatResponse chatWithWorkspace(@RequestBody AIChatRequest request) {
        validateRequest(request);
        return aiClient.chatWithWorkspace(request);
    }

    @PostMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "与工作区流式聊天", description = "向AI工作区发送消息并获得流式响应")
    public ResponseEntity<StreamingResponseBody> streamChatWithWorkspace(@RequestBody AIChatRequest request) {
        validateRequest(request);
        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = aiClient.streamChatWithWorkspace(request);
                 java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        outputStream.write("\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        outputStream.flush();
                        continue;
                    }
                    String payloadLine = line.startsWith("data:") ? line : "data: " + line;
                    outputStream.write((payloadLine + "\n\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    outputStream.flush();
                }
            } catch (Exception e) {
                try {
                    String msg = e.getMessage() == null ? "AI 流式响应失败" : e.getMessage();
                    outputStream.write(("data: {\"type\":\"error\",\"message\":\"" + msg + "\"}\n\n")
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (Exception ignored) {
                    // ignore write failure when client disconnected
                }
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(responseBody);
    }

    private void validateRequest(AIChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
}