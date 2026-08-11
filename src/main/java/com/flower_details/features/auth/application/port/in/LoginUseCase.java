package com.flower_details.features.auth.application.port.in;

import com.flower_details.features.auth.application.dto.AuthResult;
import com.flower_details.features.auth.application.dto.LoginCommand;

public interface LoginUseCase {

	AuthResult login(LoginCommand command);
}
