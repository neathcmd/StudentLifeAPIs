package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import org.mapstruct.*;

@Mapper(
        config = MapperConfiguration.class,
        uses = {RoleMapper.class }
)
public interface UserMapper {

    // ============
    // RESPONSES
    // ============
    @Mapping(target = "roles", source = "roles")
//    @Mapping(target = "profile", expression = "java(mapProfile(user.getProfile()))")
    UserResponse toUserResponse(Users user);

    @Mapping(target = "id",                 ignore = true)
    @Mapping(target = "phone",              ignore = true)
    @Mapping(target = "university",         ignore = true)
    @Mapping(target = "major",              ignore = true)
    @Mapping(target = "academicYear",       ignore = true)
    @Mapping(target = "oneSignalPlayerId",  ignore = true)
    @Mapping(target = "roles",              ignore = true)
    @Mapping(target = "isActive",           ignore = true)
    @Mapping(target = "createdAt",          ignore = true)
    @Mapping(target = "updatedAt",          ignore = true)
    Users toUserEntityCreateUser(UserCreateRequest request);

    @Mapping(target = "id",                 ignore = true)
    @Mapping(target = "phone",              ignore = true)
    @Mapping(target = "university",         ignore = true)
    @Mapping(target = "major",              ignore = true)
    @Mapping(target = "academicYear",       ignore = true)
    @Mapping(target = "oneSignalPlayerId",  ignore = true)
    @Mapping(target = "roles",              ignore = true)
    @Mapping(target = "isActive",           ignore = true)
    @Mapping(target = "createdAt",          ignore = true)
    @Mapping(target = "updatedAt",          ignore = true)
    Users toUserEntityRegisterUser(RegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",                 ignore = true)
    @Mapping(target = "username",           ignore = true)
    @Mapping(target = "email",              ignore = true)
    @Mapping(target = "password",           ignore = true)
    @Mapping(target = "academicYear",       source = "academic_year")
    @Mapping(target = "oneSignalPlayerId",  ignore = true)
    @Mapping(target = "roles",              ignore = true)
    @Mapping(target = "isActive",           ignore = true)
    @Mapping(target = "createdAt",          ignore = true)
    @Mapping(target = "updatedAt",          ignore = true)
    @Mapping(target = "authorities",        ignore = true)
    void updateUserEntity(
            UserUpdateRequest request,
            @MappingTarget Users entity
    );

}
