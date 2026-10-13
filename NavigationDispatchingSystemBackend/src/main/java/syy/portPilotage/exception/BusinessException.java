package syy.portPilotage.exception;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class BusinessException extends RuntimeException {
    

    private ErrorCode errorCode;

    private String message;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.SYSTEM_ERROR;
        this.message = message;
    }
    
    public int getCode() {
        return errorCode.getCode();
    }
}