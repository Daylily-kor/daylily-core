package com.daylily.global.response;

import com.daylily.global.response.code.BaseCode;
import com.daylily.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record SuccessResponse<T>(
        Boolean success,

        @Schema(example = "2011-12-03T10:15:30+01:00")
        String timestamp,

        @Schema(example = "200")
        int status,

        @Schema(example = "GLOBAL-200")
        String code,

        @Schema(example = "Request successful")
        String message,

        T data
) implements BaseResponse {

    public static <T> ResponseEntity<SuccessResponse<T>> of(BaseCode code, T data) {
        return BaseResponse.wrap(
                new SuccessResponse<>(
                        true,
                        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        code.status(),
                        code.code(),
                        code.message(),
                        data
                )
        );
    }

    public static ResponseEntity<SuccessResponse<Void>> of(BaseCode code) {
        return of(code, null);
    }

    public static <T> ResponseEntity<SuccessResponse<T>> ok(T data) {
        return of(SuccessCode.OK, data);
    }
}
