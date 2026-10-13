package syy.portPilotage.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import syy.portPilotage.exception.ErrorCode;
import java.io.Serializable;

@Data
@Getter
@Setter
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int code;
    private String message;
    private T data;
    private long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(int code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage());
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, data);
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(ErrorCode.SYSTEM_ERROR.getCode(), message);
    }
    
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message);
    }
    
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage());
    }
    
    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message);
    }
    
    public boolean isSuccess() {
        return this.code == ErrorCode.SUCCESS.getCode();
    }
}