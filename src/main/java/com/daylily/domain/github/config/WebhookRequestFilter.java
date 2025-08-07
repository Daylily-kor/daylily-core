package com.daylily.domain.github.config;

import com.daylily.domain.github.util.WebhookSignatureVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class WebhookRequestFilter extends OncePerRequestFilter {

    private final WebhookSignatureVerifier verifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/webhook")) {
            filterChain.doFilter(request, response);
            return;
        }

        var wrapper = new ContentCachingRequestWrapper(request);
        filterChain.doFilter(wrapper, response);

        verifier.verify(new WebhookSignatureVerifier.VerificationRequest(wrapper));
    }
}
