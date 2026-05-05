package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String fullname;
    private String phone;
    private String university;
    private String major;
    private String academic_year;
}
