package com.studentlife.StudentLifeAPIs.DTO.Response;

import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignmentMemberResponse {

    private Long id;
    private Long userId;
    private String fullname;
    private String email;
    private AssignmentMemberStatus status;
}
