package com.example.detectdanger.service.admin;

import com.example.detectdanger.dto.admin.AdminUserResponse;
import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import com.example.detectdanger.entity.Enum.UserStatus;
import com.example.detectdanger.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;

    public void deleteUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setDelete(true);
        user.setUserStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    public PageResponse<AdminUserResponse> getUsers(
            int page, int size
    ){
        validatePagination(page, size);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );
        Page<User> userPage = userRepository.findAll(pageable);

        List<AdminUserResponse> response = userPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<AdminUserResponse>builder()
                .content(response)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    private AdminUserResponse toResponse(User user) {

        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }
    }
}
