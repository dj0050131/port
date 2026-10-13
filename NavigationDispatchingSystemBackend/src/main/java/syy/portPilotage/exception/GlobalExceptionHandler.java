package syy.portPilotage.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import syy.portPilotage.common.Result;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleAuthException(AuthException e, HttpServletRequest request) {
        log.error("认证异常: URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("权限不足: URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ErrorCode.PERMISSION_DENIED);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.error("认证失败: URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ErrorCode.LOGIN_FAILED);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));
        log.error("参数校验失败: URI={}, Message={}", request.getRequestURI(), message);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
    }
    
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));
        log.error("参数绑定失败: URI={}, Message={}", request.getRequestURI(), message);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleHandlerMethodValidationException(HandlerMethodValidationException e, HttpServletRequest request) {
        // Spring 6 方法参数校验异常（包含 @Valid List<DTO> 等场景），默认消息常为 "Validation failure"
        String message = "Validation failure";
        try {
            List<String> parts = new ArrayList<>();
            e.getAllValidationResults().forEach(r -> {
                r.getResolvableErrors().forEach(err -> {
                    String m = err.getDefaultMessage();
                    if (m != null && !m.trim().isEmpty()) parts.add(m.trim());
                });
            });
            if (!parts.isEmpty()) {
                message = String.join("; ", parts);
            }
        } catch (Exception ignored) {
            // fallback to default message
            if (e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
                message = e.getMessage();
            }
        }
        log.error("参数校验失败(HandlerMethodValidationException): URI={}, Message={}", request.getRequestURI(), message);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
    }
    
    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<?> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        log.error("限流异常: URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(429, "请求过于频繁,请稍后再试");
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("非法参数: URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常,请联系管理员");
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常,请联系管理员");
    }
}