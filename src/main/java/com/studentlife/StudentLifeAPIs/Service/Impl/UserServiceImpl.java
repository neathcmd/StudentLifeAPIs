package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateUserRolesRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;
import com.studentlife.StudentLifeAPIs.Entity.Roles;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Mapper.UserMapper;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    // ─── Read ────────────────────────────────────────────────────────────────

    @Override
    public PaginatedResponse<UserResponse> getAllUsers(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Users> userPage = userRepository.findAll(pageable);

        List<UserResponse> userResponses = userPage.getContent()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();

        PaginatedResponse.PaginationMeta paginationMeta =
                new PaginatedResponse.PaginationMeta(
                        userPage.getNumber() + 1,
                        userPage.getSize(),
                        userPage.getTotalElements(),
                        userPage.getTotalPages(),
                        userPage.hasNext(),
                        userPage.hasPrevious()
                );

        return new PaginatedResponse<>(userResponses, paginationMeta);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toUserResponse(findUserOrThrow(id));
    }

    // ─── Update ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        Users user = findUserOrThrow(id);

        if (request.getFullname() != null)      user.setFullname(request.getFullname());
        if (request.getPhone() != null)         user.setPhone(request.getPhone());
        if (request.getUniversity() != null)    user.setUniversity(request.getUniversity());
        if (request.getMajor() != null)         user.setMajor(request.getMajor());
        if (request.getAcademic_year() != null) user.setAcademic_year(request.getAcademic_year());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUserRoles(Long id, UpdateUserRolesRequest request) {
        Users user = findUserOrThrow(id);

        Set<Roles> roles = request.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    // ─── Disable / Delete ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void disableUser(Long id) {
        Users user = findUserOrThrow(id);

        if (Boolean.FALSE.equals(user.getIs_active())) {
            throw new IllegalStateException("User is already disabled.");
        }

        user.setIs_active(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw notFound("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Users findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> notFound("User not found with id: " + id));
    }
}