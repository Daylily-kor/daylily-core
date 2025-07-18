package com.daylily.domain.test;

import com.daylily.domain.test.dto.TestMapper;
import com.daylily.domain.test.dto.TestRequest;
import com.daylily.domain.test.dto.TestResponse;
import com.daylily.domain.test.entity.Test;
import com.daylily.global.response.SuccessResponse;
import com.daylily.global.response.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestRepository testRepository;

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<TestResponse>> getTest(
            @PathVariable("id") Long id
    ) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("[id: " + id + "] not found"));
        TestResponse testResponse = TestMapper.INSTANCE.toResponse(test);
        return SuccessResponse.of(SuccessCode.OK, testResponse);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> createTest(
            @Valid @RequestBody TestRequest testRequest
    ) {
        testRepository.save(TestMapper.INSTANCE.toEntity(testRequest));
        return SuccessResponse.of(SuccessCode.CREATED);
    }

    @PutMapping("/{id}/description")
    public ResponseEntity<SuccessResponse<Void>> updateTestDescription(
            @PathVariable("id") Long id,
            @RequestParam("description") String description
    ) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("[id: " + id + "] not found"));

        testRepository.save(test.withDescription(description));
        return SuccessResponse.of(SuccessCode.NO_CONTENT);
    }
}
