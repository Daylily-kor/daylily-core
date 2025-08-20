package com.daylily.global.util;

import java.time.Duration;

public interface StateStore {
    /**
     * 캐시에 state 값을 저장합니다.
     * @param state 저장하고 싶은 값
     * @param ttl 저장될 값의 캐시 유효 기간
     */
    void save(String state, Duration ttl);

    void saveJwt(String state, String jwt, Duration ttl);

    /**
     * 캐시에 저장해 둔 state 값을 지웁니다.
     * @param state 지우고 싶은 값
     * @return 캐시에 state 값이 존재했고, 만료되지 않았다면 정상적으로 지워져서 true를 반환합니다.
     *         캐시에 state 값이 존재하지 않거나, 만료되었다면 false 를 반환합니다.
     */
    boolean consume(String state);

    String consumeJwt(String state);
}