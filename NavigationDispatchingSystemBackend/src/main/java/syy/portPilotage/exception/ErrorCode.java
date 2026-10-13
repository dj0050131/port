package syy.portPilotage.exception;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1002, "资源不存在"),
    ALREADY_EXISTS(1003, "资源已存在"),
    OPERATION_FAILED(1004, "操作失败"),

    UNAUTHORIZED(2000, "未授权"),
    TOKEN_INVALID(2001, "Token无效"),
    TOKEN_EXPIRED(2002, "Token已过期"),
    PERMISSION_DENIED(2003, "权限不足"),
    LOGIN_FAILED(2004, "登录失败"),
    ACCOUNT_LOCKED(2005, "账号已锁定"),
    ACCOUNT_DISABLED(2006, "账号已停用"),
    PASSWORD_ERROR(2007, "密码错误"),

    VESSEL_NOT_FOUND(3001, "船舶不存在"),
    PILOT_NOT_FOUND(3002, "引航员不存在"),
    BERTH_NOT_FOUND(3003, "泊位不存在"),
    AGENT_NOT_FOUND(3004, "代理公司不存在"),
    REQUEST_NOT_FOUND(3005, "引航申请不存在"),
    PLAN_NOT_FOUND(3006, "引航计划不存在"),
    CERTIFICATE_NOT_FOUND(3007, "签证单不存在"),
    BILLING_NOT_FOUND(3008, "计费凭证不存在"),
    VOYAGE_NOT_FOUND(3009, "航次不存在" ),
    EMAIL_ADDRESS_NOT_FOUND(3010, "未能获取到有效的邮箱地址"),

    INVALID_STATUS(4000, "状态无效"),
    STATUS_CONFLICT(4001, "状态冲突"),
    ALREADY_APPROVED(4002, "已审核"),
    ALREADY_REJECTED(4003, "已驳回"),
    ALREADY_CANCELLED(4004, "已取消"),
    CANNOT_MODIFY(4005, "无法修改"),
    CANNOT_DELETE(4006, "无法删除"),

    RATE_NOT_CONFIGURED(5001, "费率未配置"),
    BILLING_CALCULATE_FAILED(5002, "费用计算失败"),
    INVOICE_ALREADY_ISSUED(5003, "发票已开具"),
    PAYMENT_OVERDUE(5004, "付款已逾期"),

    FILE_UPLOAD_FAILED(6001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(6002, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(6003, "文件大小超限"),
    FILE_NOT_FOUND(6004, "文件不存在"),

    // 引航计划错误码
    PLAN_CREATE_FAILED(10002, "创建引航计划失败"),
    PLAN_UPDATE_FAILED(10003, "更新引航计划失败"),
    PLAN_DELETE_FAILED(10004, "删除引航计划失败"),
    PLAN_PUBLISH_FAILED(10005, "发布引航计划失败"),
    PLAN_CONFIRM_FAILED(10006, "确认引航计划失败"),
    PLAN_START_FAILED(10007, "开始引航计划失败"),
    PLAN_COMPLETE_FAILED(10008, "完成引航计划失败"),
    PLAN_CANCEL_FAILED(10009, "取消引航计划失败"),
    PLAN_STATUS_INVALID(10010, "引航计划状态无效"),
    CERTIFICATE_CREATE_FAILED(100011, "生成签证单失败"),

    // 资源错误码
    PILOT_NOT_AVAILABLE(20002, "引航员不可用"),
    DRIVER_NOT_FOUND(20003, "驾驶员不存在"),
    DRIVER_NOT_AVAILABLE(20004, "驾驶员不可用"),
    VEHICLE_NOT_FOUND(20005, "车辆不存在"),
    VEHICLE_NOT_AVAILABLE(20006, "车辆不可用"),
    TUGBOAT_NOT_FOUND(20007, "拖轮不存在"),
    TUGBOAT_NOT_AVAILABLE(20008, "拖轮不可用"),
    PILOT_BOAT_NOT_FOUND(20009, "引航艇不存在"),
    PILOT_BOAT_NOT_AVAILABLE(20010, "引航艇不可用"),
    RESOURCE_NOT_CONFIRMED(200011, "资源未确认"),
    DATA_DUPLICATION(200012, "船舶信息与其他已存在船舶重复"),

    // 分配错误码
    TUGBOAT_ASSIGN_FAILED(30001, "拖轮分配失败"),
    PILOT_BOAT_ASSIGN_FAILED(30002, "引航艇分配失败"),
    TUGBOAT_REMOVE_FAILED(30003, "拖轮移除失败"),
    PILOT_BOAT_REMOVE_FAILED(30004, "引航艇移除失败"),

    SERVER_ERROR(50001, "头像上传失败"),

    RESOURCE_TIME_CONFLICT(30005, "资源时间冲突" ),
    OPERATION_NOT_ALLOWED(30006 ,"操作不允许" ),

    // 移交
    INTERNAL_ERROR(40001, "移交过程中发生错误" ),

    DATA_EXISTS(60001, "规则类型编码已存在"),

    // 导出错误码
    EXPORT_FAILED(40001, "导出失败"),
    DATA_NOT_FOUND(7001, "数据不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}