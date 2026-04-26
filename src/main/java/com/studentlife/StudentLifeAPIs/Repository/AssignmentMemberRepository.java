package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.AssignmentMember;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentMemberRepository extends JpaRepository<AssignmentMember, Long> {

    Optional<AssignmentMember> findByAssignmentIdAndUserId(Long assignmentId, Long userId);

    List<AssignmentMember> findByAssignmentIdAndStatus(Long assignmentId, AssignmentMemberStatus status);

    // find all groups a user has accepted
    List<AssignmentMember> findByUserIdAndStatus(Long userId, AssignmentMemberStatus status);

    boolean existsByAssignmentIdAndUserId(Long assignmentId, Long userId);

    Optional<AssignmentMember> findByInviteToken(String inviteToken);

    boolean existsByAssignmentIdAndUserIdAndStatus(
            Long assignmentId, Long userId, AssignmentMemberStatus status
    );

    boolean existsByUser(Users user);
    List<AssignmentMember> findByAssignmentAndStatus(Assignments assignment, AssignmentMemberStatus status);

}
