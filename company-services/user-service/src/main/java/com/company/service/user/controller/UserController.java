package com.company.service.user.controller;

import com.company.api.user.UserDTO;
import com.company.common.core.ApiResult;
import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.user.entity.UserEntity;
import com.company.service.user.mapper.UserMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * /internal/** 只暴露给集群内部调用，网关层会拦截外部访问。
 */
@RestController
@RequestMapping("/internal/users")
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/{id}")
    public ApiResult<UserDTO> getById(@PathVariable("id") Long id) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在: " + id);
        }
        return ApiResult.ok(new UserDTO(entity.getId(), entity.getUsername(), entity.getDisplayName()));
    }
}
