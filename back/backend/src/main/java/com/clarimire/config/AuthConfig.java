package com.clarimire.config;

import com.clarimire.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class AuthConfig implements WebMvcConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // 放行登录、注册等公开接口
                String path = request.getRequestURI();
                if (path.contains("/api/users/login") || path.contains("/api/users/register") ||
                    path.contains("/api/auth/") || path.contains("/api/reservoirs") ||
                    path.contains("/api/waterSituation") || path.contains("/api/sectionMonitor")) {
                    return true;
                }

                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    if (jwtUtil.isTokenExpired(token)) {
                        response.setStatus(401);
                        response.getWriter().write("{\"code\":401,\"message\":\"Token已过期\"}");
                        return false;
                    }
                }
                return true;
            }
        }).addPathPatterns("/**");
    }
}
