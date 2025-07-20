package com.daylily.global.response;

import com.daylily.global.response.code.BaseCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record ErrorResponse<T>(
        Boolean success,

        @Schema(example = "2011-12-03T10:15:30+01:00")
        String timestamp,

        @Schema(example = "400")
        int status,

        @Schema(example = "GLOBAL-400")
        String code,

        @Schema(example = "Bad request")
        String message,

        T data
) implements BaseResponse {

    public static <T> ResponseEntity<ErrorResponse<T>> of(BaseCode e, T data) {
        return BaseResponse.wrap(
                new ErrorResponse<>(
                        false,
                        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        e.getStatus(),
                        e.getCode(),
                        e.getMessage(),
                        data
                )
        );
    }

    public static ResponseEntity<ErrorResponse<Void>> of(BaseCode e) {
        return of(e, null);
    }
}
