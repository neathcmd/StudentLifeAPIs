package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.UserCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Mapper.UserMapper;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.UserService;
import com.studentlife.StudentLifeAPIs.Utils.UserValidatorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidatorUtil userValidatorUtil;
    private final PasswordEncoder passwordEncoder;
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

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        userValidatorUtil.validateCreate(request);

        Users user = userMapper.toUserEntityCreateUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Users savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
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
        if (request.getAcademic_year() != null) user.setAcademicYear(request.getAcademic_year());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // ─── Disable / Delete ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void disableUser(Long id) {
        Users user = findUserOrThrow(id);

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException("User is already disabled.");
        }

        user.setIsActive(false);
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