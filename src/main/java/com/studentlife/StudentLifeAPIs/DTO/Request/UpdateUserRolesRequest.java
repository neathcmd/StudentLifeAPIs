package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRolesRequest {
    @NotEmpty(message = "Role list must not be empty")
    private Set<Long> roleIds;
}
