package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.OneTimeScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RecurringScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface ScheduleMapper {

    // ── Request → Entity ──────────────────────────────────────────────────────

    @Mapping(target = "type",                constant = "ONE_TIME")
    @Mapping(target = "dayOfWeek",           ignore = true)
    @Mapping(target = "recurringStartTime",  ignore = true)
    @Mapping(target = "recurringEndTime",    ignore = true)
    @Mapping(target = "id",                  ignore = true)
    @Mapping(target = "user",                ignore = true)
    @Mapping(target = "createdAt",           ignore = true)
    @Mapping(target = "updatedAt",           ignore = true)
    @Mapping(target = "assignmentId",        ignore = true)
    @Mapping(target = "isImportant",         ignore = true)
    Schedules toEntityFromOneTime(OneTimeScheduleRequest request);

    @Mapping(target = "type",         constant = "RECURRING")
    @Mapping(target = "startTime",    ignore = true)
    @Mapping(target = "endTime",      ignore = true)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "user",         ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "assignmentId", ignore = true)
    @Mapping(target = "isImportant",  ignore = true)
    Schedules toEntityFromRecurring(RecurringScheduleRequest request);

    // ── Entity → Response ─────────────────────────────────────────────────────

//    /**
//     * Maps any Schedules entity to the unified ScheduleResponse.
//     * For ONE_TIME  → startTime/endTime are populated, recurring fields are null.
//     * For RECURRING → dayOfWeek/recurringStartTime/recurringEndTime are populated, startTime/endTime are null.
//     * The frontend already handles nulls, so this is fine.
//     */
    @Mapping(source = "user.id",       target = "createdBy.id")
    @Mapping(source = "user.fullname", target = "createdBy.fullname")
    @Mapping(source = "user.username", target = "createdBy.username")
    ScheduleResponse toResponse(Schedules schedule);
}