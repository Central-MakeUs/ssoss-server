package com.ssoss.ssossbackend.shared.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/exceptions")
public class TestExceptionController {

    @GetMapping("/business")
    public void business() {
        throw new BusinessException(TestErrorCode.SAMPLE_CONFLICT);
    }

    @PostMapping("/validate")
    public void validate(@Valid @RequestBody ValidateRequest request) {
    }

    @GetMapping("/unexpected")
    public void unexpected() {
        throw new IllegalStateException("unexpected");
    }

    @GetMapping("/param")
    public void param(@RequestParam @Size(min = 2, message = "이름은 2자 이상이어야 합니다") String name) {
    }

    @GetMapping("/typed")
    public void typed(@RequestParam int number) {
    }

    public record ValidateRequest(@NotBlank(message = "이름을 입력해 주세요") String name) {
    }
}
