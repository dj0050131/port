package syy.portPilotage.enums;

import lombok.Getter;

@Getter
public enum ConfirmMethodEnum {
    WEB("WEB", "网站确认"),
    SMS("SMS", "短信确认"),
    PHONE("PHONE", "电话确认"),
    MANUAL("MANUAL", "人工确认");

    private final String code;
    private final String name;

    ConfirmMethodEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (ConfirmMethodEnum method : ConfirmMethodEnum.values()) {
            if (method.getCode().equals(code)) {
                return method.getName();
            }
        }
        return code;
    }
}