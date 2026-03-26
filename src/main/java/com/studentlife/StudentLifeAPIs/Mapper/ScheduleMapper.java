package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.OneTimeScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RecurringScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.RecurringScheduleResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.OneTimeScheduleResponse;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface ScheduleMapper {

    @Mapping(target = "type", constant = "ONE_TIME")
    @Mapping(target = "recurringStartTime", ignore = true)
    @Mapping(target = "recurringEndTime", ignore = true)
    @Mapping(target = "dayOfWeek", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Schedules toEntityFromOneTime(OneTimeScheduleRequest request);

    @Mapping(target = "type", constant = "RECURRING")
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Schedules toEntityFromRecurring(RecurringScheduleRequest request);

    @Mapping(source = "user.id", target = "createdBy.id")
    @Mapping(source = "user.username", target = "createdBy.username")
    OneTimeScheduleResponse toResponse(Schedules schedule);

    @Mapping(source = "user.id", target = "createdBy.id")
    @Mapping(source = "user.username", target = "createdBy.username")
    RecurringScheduleResponse toRecurringResponse(Schedules schedule);
}