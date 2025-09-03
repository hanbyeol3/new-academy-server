package com.academy.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Hello", description = "기본 인사 API")
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "인사 메시지", description = "간단한 인사 메시지를 반환합니다. 인증이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public String hello() {
        return "ok";
    }

}