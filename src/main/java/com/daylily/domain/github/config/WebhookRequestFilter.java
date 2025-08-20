package com.daylily.domain.github.config;

import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.util.WebhookSignatureVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class WebhookRequestFilter extends OncePerRequestFilter {

    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        public CachedBodyHttpServletRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            var byteArrayInputStream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return super.getReader();
        }
    }

    private final WebhookSignatureVerifier verifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/webhook")) {
            filterChain.doFilter(request, response);
        }
        else {
            validateWebhook(request, response, filterChain);
        }
    }

    private void validateWebhook(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        byte[] body = request.getInputStream().readAllBytes();
        String charset = Optional.ofNullable(request.getCharacterEncoding()).orElse("UTF-8");
        String payload = new String(body, charset);

        String signature = request.getHeader("X-Hub-Signature-256");
        Long targetId = Long.parseLong(request.getHeader("X-GitHub-Hook-Installation-Target-ID"));
        try {
            verifier.verify(targetId, signature, payload);
        } catch (GitHubException e) {
            String deliveryId = request.getHeader("X-GitHub-Delivery");
            String eventType = request.getHeader("X-GitHub-Event");
            log.error("Webhook verification failed for delivery ID: {}, event type: {}, target ID: {}",
                    deliveryId, eventType, targetId, e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid webhook signature");
        }

        HttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request, body);
        filterChain.doFilter(wrappedRequest, response);
    }
}
