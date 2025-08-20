package com.daylily.domain.auth.controller;

import java.util.UUID;

public record StateExchangeRequest(
        UUID state
) {
}
