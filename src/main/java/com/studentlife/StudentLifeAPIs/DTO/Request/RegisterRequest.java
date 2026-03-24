package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    private String fullname;
    private String username;
    private String email;
    private String password;
    private String phone;
    private String university;
    private String major;
    private String academicYear;
    private List<Long> roles;
}
