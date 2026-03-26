package com.studentlife.StudentLifeAPIs.Specification;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleFilter;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ScheduleSpecification {

    public static Specification<Schedules> withFilter(Long userId, ScheduleFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by userId
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (filter != null) {
                // Filter by title (case-insensitive partial match)
                if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("title")),
                            "%" + filter.getTitle().toLowerCase() + "%"
                    ));
                }

                // Filter by start date
                if (filter.getStartDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                            root.get("startDate"), filter.getStartDate()
                    ));
                }

                // Filter by end date
                if (filter.getEndDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                            root.get("endDate"), filter.getEndDate()
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}