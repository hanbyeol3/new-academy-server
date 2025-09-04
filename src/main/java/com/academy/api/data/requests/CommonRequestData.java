package com.academy.api.data.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.util.StringUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 요청 기본 클래스
 */
@SuppressWarnings("unused")
public class CommonRequestData implements ICommonRequest {

	/**
	 * 요청 객체가 null인 경우에 대한 error 코드/메시지
	 */
	public static String NullObjectErrorCode = "ECCP001";
	public static String NullObjectErrorMessage = "유효하지 않은 요청 입니다.";

	/**
	 * ValidatorFactory 객체
	 */
	private static ValidatorFactory m_validatorFactory = null;

	public static Validator getValidator() {
		if (m_validatorFactory == null) {
			m_validatorFactory = Validation.buildDefaultValidatorFactory();
		}
		return m_validatorFactory.getValidator();
	}

	/**
	 * 데이터가 유효한지 여부를 반환한다.
	 *
	 * @return 데이터가 유효한지 여부
	 */
	@Override
	@JsonIgnore
	@Schema(hidden = true)
	public boolean isInvalid() {
		boolean isValid = false;
		if (getValidator() != null) {
			Set<ConstraintViolation<CommonRequestData>> violations = getValidator().validate(this);
			isValid = violations.isEmpty();
		}
//		try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
//			Validator validator = validatorFactory.getValidator();
//			Set<ConstraintViolation<CommonRequestData>> violations = validator.validate(this);
//			isValid = violations.isEmpty();
//		}
		return !isValid;
	}

	/**
	 * 유효성 에러 코드
	 */
	private String ValidationErrorCode = "";

	/**
	 * 유효성 에러 코드를 반환한다.
	 *
	 * @return 에러 코드
	 */
	@Override
	@JsonIgnore
	@Schema(hidden = true)
	public String getValidationErrorCode() {
		if (!StringUtils.hasText(ValidationErrorCode))
			return NullObjectErrorCode;
		else
			return ValidationErrorCode;
	}

	/**
	 * 유효성 에러 코드를 설정한다.
	 *
	 * @param validationErrorCode 설정할 유효성 에러 코드
	 */
	@JsonIgnore
	@Schema(hidden = true)
	public void setValidationErrorCode(String validationErrorCode) {
		ValidationErrorCode = validationErrorCode;
	}

	/**
	 * 유효성 에러 메시지
	 */
	private String ValidationErrorMessage = "";

	/**
	 * 첫번째 유효성 에러 메시지를 반환한다.
	 *
	 * @return 유효성 에러 메시지
	 */
	@Override
	@JsonIgnore
	@Schema(hidden = true)
	public String getValidationErrorMessage() {
		String result = ValidationErrorMessage;
		// 에러코드로 설정한 내용이 없는 경우
		if (!StringUtils.hasText(ValidationErrorMessage)) {
			if (getValidator() != null) {
				Set<ConstraintViolation<CommonRequestData>> violations = getValidator().validate(this);

				List<ConstraintViolation<CommonRequestData>> violationList = new ArrayList<>(violations);
				if (!violationList.isEmpty())
					result = violationList.get(violationList.size() - 1).getMessage();
			}
//				Validator validator = validatorFactory.getValidator();
//
//				Set<ConstraintViolation<CommonRequestData>> violations = validator.validate(this);
//
//				List<ConstraintViolation<CommonRequestData>> violationList = new ArrayList<>(violations);
//				if (!violationList.isEmpty())
//					result = violationList.get(violationList.size() - 1).getMessage();
//			}
		}
		return result;
	}

	/**
	 * 유효성 에러 메시지를 설정한다.
	 *
	 * @param validationErrorMessage 설정할 유효성 에러 메시지
	 */
	@JsonIgnore
	@Schema(hidden = true)
	public void setValidationErrorMessage(String validationErrorMessage) {
		ValidationErrorMessage = validationErrorMessage;
	}

	/**
	 * 전체 유효성 에러 메시지 목록을 반환한다.
	 *
	 * @return 전체 유효성 에러 메시지 목록
	 */
	@Override
	@JsonIgnore
	@Schema(hidden = true)
	public List<String> getValidationErrorMessages() {
		List<String> result = new ArrayList<>();

		// 에러코드로 설정한 내용이 없는 경우
		if (!StringUtils.hasText(ValidationErrorMessage)) {
			if (getValidator() != null) {
				Set<ConstraintViolation<CommonRequestData>> violations = getValidator().validate(this);

				List<ConstraintViolation<CommonRequestData>> violationList = new ArrayList<>(violations);
				for (int index = violationList.size() - 1; index >= 0; index--)
					result.add(violationList.get(index).getMessage());
			}
//			try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
//				Validator validator = validatorFactory.getValidator();
//
//				Set<ConstraintViolation<CommonRequestData>> violations = validator.validate(this);
//
//				List<ConstraintViolation<CommonRequestData>> violationList = new ArrayList<>(violations);
//				for (int index = violationList.size() - 1; index >= 0; index--)
//					result.add(violationList.get(index).getMessage());
//			}
		}
		return result;
	}
}
