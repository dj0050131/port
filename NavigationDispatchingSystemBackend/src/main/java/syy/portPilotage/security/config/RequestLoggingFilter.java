package syy.portPilotage.security.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        System.out.println("=== 请求进入: " + method + " " + requestURI + " ===");

        chain.doFilter(request, response);

        System.out.println("=== 请求完成: " + method + " " + requestURI + " ===");
    }
}