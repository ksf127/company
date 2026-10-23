package com.company.api.user;

/**
 * 跨服务传输对象。契约包内只允许出现不可变 DTO。
 */
public record UserDTO(Long id, String username, String displayName) {
}
