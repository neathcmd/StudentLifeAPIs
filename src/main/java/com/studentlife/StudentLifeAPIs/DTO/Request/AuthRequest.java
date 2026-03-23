package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.Data;

@Data
public class AuthRequest {

    private String email_or_username;
    private String password;
}
