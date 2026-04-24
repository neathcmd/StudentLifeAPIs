package com.studentlife.StudentLifeAPIs.Specification;

import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleSpecification {

//    /**
//     * Builds a query for all schedules belonging to a user,
//     * optionally filtered by a date range (startDate to endDate).
//     *
//     * How date filtering works:
//     * - ONE_TIME  → included if its startTime falls within [startDate, endDate]
//     * - RECURRING → always included (they repeat every week, no specific date)
//     *
//     * If no date range is given, all schedules for the user are returned.
//     */
    public static Specification<Schedules> forUser(Long userId, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by the authenticated user
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Apply date range only if both dates are provided
            if (startDate != null && endDate != null) {
                LocalDateTime from = startDate.atStartOfDay();
                LocalDateTime to   = endDate.atTime(23, 59, 59);

                // ONE_TIME: show if the event overlaps with the requested date range
                Predicate isOneTime = cb.equal(root.get("type"), ScheduleType.ONE_TIME);
                Predicate overlaps = cb.and(
                        cb.lessThanOrEqualTo(root.get("startTime"), to),    // starts before range ends
                        cb.greaterThanOrEqualTo(root.get("endTime"), from)  // ends after range starts
                );
                Predicate oneTimeInRange = cb.and(isOneTime, overlaps);

                // RECURRING: always show regardless of date range
                Predicate isRecurring = cb.equal(root.get("type"), ScheduleType.RECURRING);

                // Either recurring OR one-time-in-range
                predicates.add(cb.or(isRecurring, oneTimeInRange));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}