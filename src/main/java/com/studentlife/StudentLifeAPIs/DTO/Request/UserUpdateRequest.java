package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullname;

    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Size(max = 150, message = "University name must not exceed 150 characters")
    private String university;

    @Size(max = 100, message = "Major must not exceed 100 characters")
    private String major;

    @Size(max = 20, message = "Academic year must not exceed 20 characters")
    private String academic_year;
}
