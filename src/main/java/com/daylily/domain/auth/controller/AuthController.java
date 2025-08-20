package com.daylily.domain.auth.controller;

import com.daylily.global.response.ErrorResponse;
import com.daylily.global.response.SuccessResponse;
import com.daylily.global.response.code.ErrorCode;
import com.daylily.global.util.StateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final StateStore stateStore;

    @PostMapping("/exchange")
    public ResponseEntity<?> exchangeStateForJwt(
            @RequestBody StateExchangeRequest request) {

        log.debug("Exchanging state for JWT: {}", request.state());

        String jwt = stateStore.consumeJwt(request.state().toString());

        log.debug("JWT retrieved for state {}: {}", request.state(), jwt);

        if (jwt != null) {
            return SuccessResponse.ok(Map.of("jwt", jwt));
        }
        else {
            return ErrorResponse.of(ErrorCode.ACCESS_DENIED);
        }
    }
}
