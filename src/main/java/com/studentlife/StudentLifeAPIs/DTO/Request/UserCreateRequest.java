package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String fullname;
    private String username;
    private String email;
    private String password;
}
