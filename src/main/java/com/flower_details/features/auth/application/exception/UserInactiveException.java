package com.flower_details.features.auth.application.exception;

public class UserInactiveException extends RuntimeException {

	public UserInactiveException() {
		super("El usuario se encuentra inactivo");
	}
}
