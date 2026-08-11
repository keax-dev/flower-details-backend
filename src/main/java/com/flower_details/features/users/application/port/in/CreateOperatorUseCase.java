package com.flower_details.features.users.application.port.in;

import com.flower_details.features.users.application.dto.CreateOperatorCommand;
import com.flower_details.features.users.application.dto.UserProfile;

public interface CreateOperatorUseCase {

	UserProfile createOperator(CreateOperatorCommand command);
}
