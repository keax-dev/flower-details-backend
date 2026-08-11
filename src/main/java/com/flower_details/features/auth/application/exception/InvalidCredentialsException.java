package com.flower_details.features.auth.application.exception;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Correo o contrasena incorrectos");
	}
}
