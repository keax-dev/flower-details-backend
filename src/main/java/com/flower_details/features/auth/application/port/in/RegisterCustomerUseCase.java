package com.flower_details.features.auth.application.port.in;

import com.flower_details.features.auth.application.dto.AuthResult;
import com.flower_details.features.auth.application.dto.RegisterCustomerCommand;

public interface RegisterCustomerUseCase {

	AuthResult registerCustomer(RegisterCustomerCommand command);
}
