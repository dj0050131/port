package syy.portPilotage.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomCorsFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String origin = httpRequest.getHeader("Origin");
        if (origin != null && (origin.equals("http://localhost:4000") ||
                origin.equals("http://127.0.0.1:4000") ||
                origin.equals("http://localhost:4060") ||
                origin.equals("http://127.0.0.1:4060"))) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        }
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        httpResponse.setHeader(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization, X-Requested-With, X-Request-Timestamp"
        );

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(request, response);
        }
    }
}
