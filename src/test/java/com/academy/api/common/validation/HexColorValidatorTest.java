package com.academy.api.common.validation;

import com.academy.api.schedule.domain.ScheduleCategory;
import com.academy.api.schedule.dto.RequestAcademicScheduleCreate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hex 색상 검증 테스트")
class HexColorValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"#000000", "#FFFFFF", "#123456", "#abcdef", "#ABCDEF", "#ff0000", "#00FF00"})
    @DisplayName("유효한 hex 색상 코드는 검증에 성공한다")
    void validateValidHexColors_Success(String color) {
        // given
        RequestAcademicScheduleCreate request = createValidRequest();
        request.setColor(color);

        // when
        Set<ConstraintViolation<RequestAcademicScheduleCreate>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"000000", "#12345", "#1234567", "#GGGGGG", "#12345G", "red", "blue", "#"})
    @DisplayName("유효하지 않은 hex 색상 코드는 검증에 실패한다")
    void validateInvalidHexColors_Fail(String color) {
        // given
        RequestAcademicScheduleCreate request = createValidRequest();
        request.setColor(color);

        // when
        Set<ConstraintViolation<RequestAcademicScheduleCreate>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        ConstraintViolation<RequestAcademicScheduleCreate> violation = violations.iterator().next();
        assertThat(violation.getMessage()).contains("유효하지 않은 색상 코드");
    }

    @Test
    @DisplayName("색상이 null이면 검증을 통과한다")
    void validateNullColor_Success() {
        // given
        RequestAcademicScheduleCreate request = createValidRequest();
        request.setColor(null);

        // when
        Set<ConstraintViolation<RequestAcademicScheduleCreate>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("색상이 빈 문자열이면 검증을 통과한다")
    void validateEmptyColor_Success() {
        // given
        RequestAcademicScheduleCreate request = createValidRequest();
        request.setColor("");

        // when
        Set<ConstraintViolation<RequestAcademicScheduleCreate>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("색상이 공백만 있으면 검증을 통과한다")
    void validateBlankColor_Success() {
        // given
        RequestAcademicScheduleCreate request = createValidRequest();
        request.setColor("   ");

        // when
        Set<ConstraintViolation<RequestAcademicScheduleCreate>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("앞뒤 공백이 있는 유효한 hex 색상은 검증에 성공한다")
    void validateTrimmedValidHexColor_Success() {
        // given
        RequestAcademicScheduleCreate request = createValidRequest();
        request.setColor("  #FF0000  ");

        // when
        Set<ConstraintViolation<RequestAcademicScheduleCreate>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    private RequestAcademicScheduleCreate createValidRequest() {
        RequestAcademicScheduleCreate request = new RequestAcademicScheduleCreate();
        request.setCategory(ScheduleCategory.EXAM);
        request.setStartDate(LocalDate.of(2025, 9, 4));
        request.setEndDate(LocalDate.of(2025, 9, 4));
        request.setTitle("테스트 일정");
        request.setPublished(true);
        return request;
    }
}