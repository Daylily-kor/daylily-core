package com.daylily.domain.dashboard.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public enum ContainerStateType {
    CREATED("created"),
    RUNNING("running"),
    PAUSED("paused"),
    RESTARTING("restarting"),
    EXITED("exited"),
    REMOVING("removing"),
    DEAD("dead");

    @Getter
    private final String status;

    private static final Map<String, ContainerStateType> STATUS_MAP = Map.of(
            "created", CREATED,
            "running", RUNNING,
            "paused", PAUSED,
            "restarting", RESTARTING,
            "exited", EXITED,
            "removing", REMOVING,
            "dead", DEAD
    );

    public static ContainerStateType fromString(String status) {
        return STATUS_MAP.getOrDefault(status, DEAD);
    }
}
