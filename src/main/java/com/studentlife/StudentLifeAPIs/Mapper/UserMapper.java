package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

}
