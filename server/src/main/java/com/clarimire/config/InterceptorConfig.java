package com.clarimire.config;

import com.clarimire.util.AuthContext;
import com.clarimire.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    private boolean isPublicPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        return "/auth/login".equals(uri)
                || "/auth/register".equals(uri)
                || uri.startsWith("/uploads/")
                || uri.startsWith("/public/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    return true;
                }

                if (isPublicPath(request)) {
                    return true;
                }

                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    try {
                        String username = jwtUtil.getUsernameFromToken(token);
                        if (jwtUtil.validateToken(token, username)) {
                            Claims claims = jwtUtil.getClaimsFromToken(token);
                            AuthContext.setFromClaims(request, claims);
                            return true;
                        }
                    } catch (Exception ignored) {
                        // fall through to unauthorized
                    }
                }

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"未授权\"}");
                return false;
            }
        })
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/auth/login",
            "/auth/register",
            "/uploads/**",
            "/public/**"
        );
    }
}
