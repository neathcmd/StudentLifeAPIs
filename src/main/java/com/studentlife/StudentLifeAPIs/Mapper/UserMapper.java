package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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

    Users toUserEntityCreateUser(UserCreateRequest request);

    Users toUserEntityRegisterUser(RegisterRequest request);

    void updateUserEntity(
            UserUpdateRequest request,
            @MappingTarget Users entity
    );

}
