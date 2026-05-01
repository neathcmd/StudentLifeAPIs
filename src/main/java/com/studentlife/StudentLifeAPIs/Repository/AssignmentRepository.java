package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignments, Long> {

    @Query(value = """
    SELECT a.* FROM assignments a
    WHERE a.user_id = :userId
    OR EXISTS (
        SELECT 1 FROM assignment_members m
        WHERE m.assignment_id = a.id
        AND m.user_id = :userId
        AND m.status = 'ACCEPTED'
    )
""", nativeQuery = true)
    List<Assignments> findAllAccessibleByUserId(
            @Param("userId") Long userId
    );

    boolean existsByUser(Users user);

    List<Assignments> findByUser(Users user);

//    List<Assignments> findByUserId(Long userId);

    /**
     * Find all active assignments whose due date falls within a time window.
     * Used by the reminder scheduler to find assignments due in ~72h, ~24h, ~2h.
     *
     * Excludes COMPLETED assignments — no point reminding for finished work.
     */
    @Query("SELECT a FROM Assignments a WHERE a.dueDate BETWEEN :from AND :to AND a.status != :excludeStatus")
    List<Assignments> findDueWithin(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("excludeStatus") AssignmentStatus excludeStatus
    );
}
