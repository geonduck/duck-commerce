package kr.hhplus.be.server.config.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        if(session != null) {
            log.debug("SESSION ID: [{}]", session.getId());
        }

        log.debug("[REQUEST] URI: {}, Method: {}, RemoteAddr: {}",
                request.getRequestURI(), request.getMethod(), request.getRemoteAddr());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug("[RESPONSE] URI: {}, Method: {}, Status: {}",
                request.getRequestURI(), request.getMethod(), response.getStatus());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if(ex != null) {
            log.error("[ERROR] URI: {}, Method: {}, Exception: {}",
                    request.getRequestURI(), request.getMethod(), ex.getMessage(), ex);
        } else {
            log.info("[COMPLETED] URI: {}, Method: {}, Status: {}",
                    request.getRequestURI(), request.getMethod(), response.getStatus());
        }
    }
}
