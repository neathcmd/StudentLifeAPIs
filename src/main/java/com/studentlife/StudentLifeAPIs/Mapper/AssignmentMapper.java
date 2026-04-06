package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface AssignmentMapper {

    // ── Request → Entity ──────────────────────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "user",      ignore = true)
    @Mapping(target = "status",    ignore = true)   // defaults to PENDING in entity
    @Mapping(target = "progress",  ignore = true)   // defaults to 0 in entity
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Assignments toEntity(CreateAssignmentRequest request);

    // ── Entity → Response ─────────────────────────────────────────────────────

    AssignmentResponse toResponse(Assignments assignment);
}