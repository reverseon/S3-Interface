package rev.util.s3interfacesb.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
@Slf4j
public class MiddlewareHandler implements HandlerInterceptor {
    private final String trueToken;

    public MiddlewareHandler(
            @Value("${web.secret_access_token}") String trueToken
    ) {
        this.trueToken = trueToken;
    }
    private HandlerMethod getHandlerMethod(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod;
        }
        return null;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = getHandlerMethod(handler);
        if (handlerMethod != null && handlerMethod.getMethod().getName().equals("uploadPOST")) {
            String AUTH_HEADER = "Authorization";
            String authHeader = request.getHeader(AUTH_HEADER);
            String BEARER_PREFIX = "Bearer ";
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return false;
            }
            String token = authHeader.substring(BEARER_PREFIX.length());
            if (!token.equals(trueToken)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return false;
            }
            return true;
        }
        return true;
    }
}
