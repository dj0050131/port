package syy.portPilotage.enums;

import lombok.Getter;

@Getter
public enum PlanStatusEnum {

    PLANNING("PLANNING", "计划中", "未计划，未发布"),
    PLANNED("PLANNED", "已计划", "已计划，未发布"),
    REPLANNED("REPLANNED", "重计划", "计划已重新计划，未发布"),
    REPLAN_REQUIRED("REPLAN_REQUIRED", "待重排", "资源取消/系统原因导致计划不可继续，需重排后再发布"),
    PUBLISHED("PUBLISHED", "已发布", "计划已发布给相关人员"),
    CONFIRMED("CONFIRMED", "已确认", "各资源接收到发布信息且确认完毕"),
    ONGOING("ONGOING", "进行中", "引航作业已开始"),
    COMPLETED("COMPLETED", "已完成", "引航作业已完成"),
    CERTIFICATED("CERTIFICATED", "已签证", "引航作业已签证"),
    BILLED("BILLED", "已计费", "引航作业已计费"),
    PAID("PAID", "已付款", "引航作业已完成"),

    CANCELLED("CANCELLED", "已取消", "计划已取消");

    private final String code;
    private final String name;
    private final String description;

    PlanStatusEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static PlanStatusEnum fromCode(String code) {
        for (PlanStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}