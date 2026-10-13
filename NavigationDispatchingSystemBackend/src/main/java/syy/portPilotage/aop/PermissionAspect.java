package syy.portPilotage.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import syy.portPilotage.exception.AuthException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.util.SecurityUtil;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    
    @Pointcut("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    public void permissionPointcut() {
    }
    
    @Around("permissionPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!SecurityUtil.isAuthenticated()) {
            log.error("用户未认证");
            throw new AuthException(ErrorCode.UNAUTHORIZED);
        }
        return joinPoint.proceed();
    }
}
