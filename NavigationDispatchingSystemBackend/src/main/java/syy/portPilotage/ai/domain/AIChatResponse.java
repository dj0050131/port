package syy.portPilotage.ai.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {
    
    private String id;
    private String type;
    private String textResponse;
    private List<Source> sources;
    private boolean close;
    private String error;
    private List<Attachment> attachments;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Source {
        private String title;
        private String chunk;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String name;
        private String mime;
        private String contentString;
    }
}