package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignments, Long> {

    List<Assignments> findByUserId(Long userId);

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
