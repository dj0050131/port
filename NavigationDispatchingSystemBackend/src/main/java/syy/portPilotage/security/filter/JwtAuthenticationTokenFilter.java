package syy.portPilotage.security.filter;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import syy.portPilotage.constant.SecurityConstant;
import syy.portPilotage.jwt.JwtTokenUtil;
import syy.portPilotage.redis.service.UserAuthService;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    UserAuthService userAuthService;
    public JwtAuthenticationTokenFilter() {
        log.info("JwtAuthenticationTokenFilter 被创建");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        
        if (StrUtil.isNotBlank(token) && jwtTokenUtil.validateToken(token)) {
            String account = jwtTokenUtil.getAccountFromToken(token);

            String redisToken = userAuthService.getUserToken(account);
            if (redisToken == null || !redisToken.equals(token)) {
                log.warn("Token在Redis中不存在或不匹配: username={}", account);
                filterChain.doFilter(request, response);
                return;
            }
            
            if (StrUtil.isNotBlank(account) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(account);  //龙王标记 重写逻辑
                
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("已认证用户: {}, URI: {}", account, request.getRequestURI());
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstant.TOKEN_HEADER);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(SecurityConstant.TOKEN_PREFIX)) {
            return bearerToken.substring(SecurityConstant.TOKEN_PREFIX.length());
        }
        return null;
    }
}