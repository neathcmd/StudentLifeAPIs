package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberResponse {

    private Long   id;
    private String fullname;
    private String username;
    private String email;
    private String university;
    private String major;
    private String academicYear;

    private boolean online;
}
