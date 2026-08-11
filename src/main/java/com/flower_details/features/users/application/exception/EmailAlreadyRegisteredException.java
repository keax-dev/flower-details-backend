package com.flower_details.features.users.application.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

	public EmailAlreadyRegisteredException(String email) {
		super("Ya existe un usuario registrado con el correo " + email);
	}
}
