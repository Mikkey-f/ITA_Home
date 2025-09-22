package com.ita.home.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ita.home.annotation.RequireAuth;
import com.ita.home.result.Result;
import com.ita.home.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * JWT身份验证过滤器
 * 检查标注了@RequireAuth注解的接口，验证请求头中的JWT令牌
 * 如果验证失败，返回401错误；如果验证成功，将用户信息存储到请求属性中
 * 
 * @author ITA Team
 * @since 2025-09-22
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("JWT过滤器处理请求: {} {}", method, requestURI);

        try {
            // 检查是否需要JWT验证
            RequireAuth requireAuth = getRequireAuthAnnotation(request);
            
            if (requireAuth == null) {
                // 不需要验证，直接放行
                log.debug("接口不需要JWT验证: {} {}", method, requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // 从请求头获取JWT令牌
            String authHeader = request.getHeader(jwtUtil.getHeaderName());
            String token = jwtUtil.extractTokenFromHeader(authHeader);

            if (token == null || token.trim().isEmpty()) {
                if (requireAuth.required()) {
                    // 必须登录但没有提供令牌
                    log.warn("访问需要验证的接口但未提供JWT令牌: {} {}", method, requestURI);
                    sendUnauthorizedResponse(response, "请先登录");
                } else {
                    // 可选登录，没有令牌时直接放行
                    log.debug("可选登录接口，未提供JWT令牌: {} {}", method, requestURI);
                    filterChain.doFilter(request, response);
                }
                return;
            }

            // 验证JWT令牌
            if (!jwtUtil.validateToken(token)) {
                log.warn("JWT令牌验证失败: {} {}", method, requestURI);
                sendUnauthorizedResponse(response, "登录已过期，请重新登录");
                return;
            }

            // 令牌验证成功，提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            // 将用户信息存储到请求属性中，供Controller使用
            request.setAttribute("currentUserId", userId);
            request.setAttribute("currentUsername", username);
            request.setAttribute("jwtToken", token);

            log.debug("JWT验证成功，用户: {} (ID: {})", username, userId);

            // 继续处理请求
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT过滤器处理请求时发生异常: {} {}", method, requestURI, e);
            sendUnauthorizedResponse(response, "身份验证失败");
        }
    }

    /**
     * 获取接口方法上的@RequireAuth注解
     */
    private RequireAuth getRequireAuthAnnotation(HttpServletRequest request) {
        try {
            HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);
            if (handlerChain != null && handlerChain.getHandler() instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();
                Method method = handlerMethod.getMethod();
                
                // 先检查方法上的注解
                RequireAuth methodAuth = method.getAnnotation(RequireAuth.class);
                if (methodAuth != null) {
                    return methodAuth;
                }
                
                // 再检查类上的注解
                Class<?> handlerClass = handlerMethod.getBeanType();
                return handlerClass.getAnnotation(RequireAuth.class);
            }
        } catch (Exception e) {
            log.debug("获取@RequireAuth注解时发生异常，跳过JWT验证", e);
        }
        return null;
    }

    /**
     * 发送401未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<String> result = Result.error(message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonResponse);
            writer.flush();
        }
        
        log.debug("发送401响应: {}", message);
    }

    /**
     * 跳过某些不需要处理的请求
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 跳过静态资源
        if (path.startsWith("/static/") || 
            path.startsWith("/css/") || 
            path.startsWith("/js/") || 
            path.startsWith("/images/") ||
            path.startsWith("/favicon.ico")) {
            return true;
        }
        
        // 跳过Swagger相关路径
        if (path.startsWith("/swagger-ui") || 
            path.startsWith("/v3/api-docs") ||
            path.equals("/")) {
            return true;
        }
        
        // 跳过健康检查等监控端点
        if (path.startsWith("/actuator/")) {
            return true;
        }
        
        return false;
    }
}