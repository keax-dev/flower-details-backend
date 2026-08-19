package com.flower_details.shared.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

	private boolean requireStrength;

	@Override
	public void initialize(StrongPassword constraintAnnotation) {
		requireStrength = constraintAnnotation.requireStrength();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}
		if (value.getBytes(StandardCharsets.UTF_8).length > 72) {
			return false;
		}
		if (!requireStrength) {
			return true;
		}

		return value.codePointCount(0, value.length()) >= 10
				&& value.codePoints().anyMatch(Character::isUpperCase)
				&& value.codePoints().anyMatch(Character::isLowerCase)
				&& value.codePoints().anyMatch(Character::isDigit);
	}
}
