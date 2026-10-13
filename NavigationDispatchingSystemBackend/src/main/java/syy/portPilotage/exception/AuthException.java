package syy.portPilotage.exception;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class AuthException extends RuntimeException {
    

    private ErrorCode errorCode;

    private String message;
    
    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
    
    public AuthException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public int getCode() {
        return errorCode.getCode();
    }
}