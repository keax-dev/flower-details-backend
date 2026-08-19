package com.flower_details.shared.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

	String message() default "La contrasena debe tener al menos 10 caracteres, incluir mayuscula, minuscula y numero, "
			+ "y no superar 72 bytes";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean requireStrength() default true;
}
