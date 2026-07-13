package com.argus.response;

import com.argus.entity.User;
import com.argus.enums.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean emailVerified,
        boolean enabled
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified(),
                user.isEnabled()
        );
    }
}
