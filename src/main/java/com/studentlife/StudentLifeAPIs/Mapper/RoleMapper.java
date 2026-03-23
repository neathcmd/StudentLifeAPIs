package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.Entity.Roles;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(config = MapperConfiguration.class)
public interface RoleMapper {

    // Entity → Response
    default String map(Roles role) {
        return role.getName();
    }

    // Request → Entity (used by MapStruct internally)
    default Roles map(Long id) {
        Roles role = new Roles();
        role.setId(id);
        return role;
    }
}