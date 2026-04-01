package com.studentlife.StudentLifeAPIs.DTO.Response;


import com.studentlife.StudentLifeAPIs.Entity.Roles;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String fullname;
    private String username;
    private String email;
    private String phone;
    private String university;
    private String major;
    private String academicYear;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
