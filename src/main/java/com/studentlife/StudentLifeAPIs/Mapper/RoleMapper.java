package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.Entity.Roles;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(config = MapperConfiguration.class)
public interface RoleMapper {

//    default String map(Roles role) {
//        return role.getName();
//    }

    default List<String> map(Set<Roles> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(Roles::getName)
                .toList();
    }
}
