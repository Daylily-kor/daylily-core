package com.daylily.domain.dashboard.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public enum ContainerStatusType {
    CREATED("created"),
    RUNNING("running"),
    PAUSED("paused"),
    RESTARTING("restarting"),
    EXITED("exited"),
    REMOVING("removing"),
    DEAD("dead");

    @Getter
    private final String status;

    private static final Map<String, ContainerStatusType> STATUS_MAP = Map.of(
            "created", CREATED,
            "running", RUNNING,
            "paused", PAUSED,
            "restarting", RESTARTING,
            "exited", EXITED,
            "removing", REMOVING,
            "dead", DEAD
    );

    public static ContainerStatusType fromString(String status) {
        return STATUS_MAP.getOrDefault(status, null);
    }
}
