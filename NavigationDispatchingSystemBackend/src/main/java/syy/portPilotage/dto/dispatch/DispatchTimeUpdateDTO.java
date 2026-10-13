package syy.portPilotage.dto.dispatch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import syy.portPilotage.util.jackson.MultiFormatDateDeserializer;

import java.util.Date;

@Data
public class DispatchTimeUpdateDTO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date boardingTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date leavingTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date actualStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date actualEndTime;
    private String notes;
}