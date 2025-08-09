package com.daylily.domain.github.util;

import com.daylily.domain.github.action_type.GitHubWebhookActionType;
import com.daylily.domain.github.action_type.InstallationActionType;
import com.daylily.domain.github.action_type.PullRequestActionType;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public abstract class ActionTypeChecker {

    private static final ConcurrentMap<Class<?>, Map<String, ? extends GitHubWebhookActionType>> ACTION_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 주어진 문자열을 Enum 타입으로 변환합니다.
     * @param action 변환할 문자열
     * @param enumType 변환할 Enum 상수의 클래스
     * @return 주어진 문자열에 해당하는 Enum 값
     * @param <ActionType> 변환할 Enum 상수를 나타내는 제너릭 타입
     */
    @SuppressWarnings("unchecked")
    public static <ActionType extends GitHubWebhookActionType> ActionType fromString(String action, Class<ActionType> enumType) {
        Map<String, ActionType> lookup = (Map<String, ActionType>) ACTION_TYPE_CACHE.computeIfAbsent(
                enumType,
                (Class<?> ignore) -> Arrays.stream(enumType.getEnumConstants())
                        .collect(Collectors.toMap(
                                (ActionType actionType) -> actionType.action().toLowerCase(),
                                (ActionType actionType) -> actionType
                        ))
        );

        ActionType result = lookup.get(action.toLowerCase());
        if (result == null) {
            throw new IllegalArgumentException("Unknown action type: " + action);
        }
        return result;
    }

    public static InstallationActionType fromInstallationActionString(String action) {
        return fromString(action, InstallationActionType.class);
    }

    public static PullRequestActionType fromPullRequestActionString(String action) {
        return fromString(action, PullRequestActionType.class);
    }
}
