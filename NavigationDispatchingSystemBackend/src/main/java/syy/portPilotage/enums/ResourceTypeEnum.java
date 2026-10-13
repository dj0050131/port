package syy.portPilotage.enums;

import lombok.Getter;

@Getter
public enum ResourceTypeEnum {
    PILOT("PILOT", "引航员"),
    DRIVER("DRIVER", "驾驶员"),
    PILOT_BOAT("PILOT_BOAT", "引航艇"),
    TUGBOAT("TUGBOAT", "拖轮"),
    VEHICLE("VEHICLE", "车辆"),
    OVERALL("OVERALL", "整体计划");

    private final String code;
    private final String name;

    ResourceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (ResourceTypeEnum type : ResourceTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type.getName();
            }
        }
        return code;
    }
}