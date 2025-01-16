package kr.hhplus.be.server.config.web;

import jakarta.servlet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@WebFilter(urlPatterns = "/api/v1/*")
public class LogFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Log Filter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uuid = UUID.randomUUID().toString();
        MDC.put("logId", uuid);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // 요청-응답 완료 후 정리
        }
    }

    @Override
    public void destroy() {
        log.info("Log Filter destroy");
    }
}