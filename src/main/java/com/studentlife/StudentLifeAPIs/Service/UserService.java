package com.studentlife.StudentLifeAPIs.Service;


import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateUserRolesRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;

public interface UserService {

    PaginatedResponse<UserResponse> getAllUsers(int page, int size);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    UserResponse updateUserRoles(Long id, UpdateUserRolesRequest request);

    void disableUser(Long id);

    void deleteUser(Long id);
}