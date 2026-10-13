package syy.portPilotage.security.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import syy.portPilotage.constant.SecurityConstant;
import syy.portPilotage.security.component.CustomAccessDeniedHandler;
import syy.portPilotage.security.component.CustomAuthenticationEntryPoint;
import syy.portPilotage.security.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // 暂时注释掉有问题的自动注入
     @Autowired
     JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
     @Autowired
     CustomAccessDeniedHandler customAccessDeniedHandler;
     @Autowired
     CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Autowired
    UserDetailsService userDetailsService;

    public SecurityConfig() {
        System.out.println("🚀🚀🚀 SecurityConfig 构造函数被调用 🚀🚀🚀");
    }

    @PostConstruct
    public void init() {
        System.out.println("✅✅✅ SecurityConfig @PostConstruct 初始化完成 ✅✅✅");
    }

//    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstant.WHITE_LIST).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
//    @Bean
//
//        http
//
//                    }
//
//                })
//
//    }
}