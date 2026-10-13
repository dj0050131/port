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
public class AIChatRequest {
    private String message;
    private String mode;
    private String sessionId;
    private Boolean reset;
    private List<AIChatResponse.Attachment> attachments;
}
