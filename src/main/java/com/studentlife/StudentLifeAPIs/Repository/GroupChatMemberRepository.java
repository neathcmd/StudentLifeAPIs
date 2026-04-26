package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.GroupChatMember;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatMemberRepository extends JpaRepository<GroupChatMember, Long> {
    List<GroupChatMember> findByAssignmentId(Long assignmentId);
    boolean existsByAssignmentIdAndUserId(Long assignmentId, Long userId);

    boolean existsByUser(Users user);
}
