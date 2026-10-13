package syy.portPilotage.ai.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StreamChatChunk {
    private String id;
    private String type; // "abort" 或 "textResponseChunk"

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("textResponse")
    private String textResponse;

    private List<AIChatResponse.Source> sources;
    private Boolean close;
    private Object error;
}