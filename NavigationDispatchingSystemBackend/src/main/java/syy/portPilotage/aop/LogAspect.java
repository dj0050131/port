package syy.portPilotage.aop;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import syy.portPilotage.util.SecurityUtil;

@Slf4j
@Data
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Pointcut("execution(* syy.portPilotage.controller..*.*(..))")
    public void controllerPointcut() {
    }
    
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String uri = request.getRequestURI();
            String method = request.getMethod();
            String username = SecurityUtil.getCurrentUsername();
            
            Object[] args = joinPoint.getArgs();
            String params = "";
            if (args != null && args.length > 0) {
                try {
                    params = objectMapper.writeValueAsString(args);
                    if (params.length() > 500) {
                        params = params.substring(0, 500) + "...";
                    }
                } catch (Exception e) {
                    params = "[无法序列化]";
                }
            }
            
            log.info("==> 请求开始: URI={}, Method={}, User={}, Params={}", 
                    uri, method, StrUtil.blankToDefault(username, "匿名"), params);
        }
        
        Object result = joinPoint.proceed();
        
        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;
        
        log.info("<== 请求结束: Cost={}ms", costTime);
        
        return result;
    }
    
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String uri = request.getRequestURI();
            log.error("<== 请求异常: URI={}, Exception={}", uri, ex.getMessage(), ex);
        }
    }
}