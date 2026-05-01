package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedules, Long>,
        JpaSpecificationExecutor<Schedules> {

    boolean existsByUser(Users user);

    boolean existsByTitleAndUserId(String title, Long userId);

    void deleteByAssignmentId(Long assignmentId);
}