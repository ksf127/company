package com.company.service.system.service;

import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.system.entity.SysUser;
import com.company.service.system.repository.SysUserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 纯 Mockito 单测：不启动 Spring、不连接 MySQL，覆盖用户创建的核心分支。
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceTest {

    @Mock
    private SysUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserService userService;

    @Test
    void createShouldRejectDuplicateUserName() {
        SysUser request = new SysUser();
        request.setUserName("admin");
        when(userRepository.findByUserName("admin")).thenReturn(new SysUser());

        assertThatThrownBy(() -> userService.create(request, List.of(1L)))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> assertThat(((BizException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT))
                .hasMessageContaining("登录账号已存在");

        verify(userRepository, never()).insert(any());
    }

    @Test
    void createShouldEncodeDefaultPasswordAndAssignRoles() {
        SysUser request = new SysUser();
        request.setUserName("zhangsan");
        when(userRepository.findByUserName("zhangsan")).thenReturn(null);
        when(passwordEncoder.encode(SysUserService.DEFAULT_PASSWORD)).thenReturn("encoded-password");
        // 真实 MyBatis 会在 insert 后回填主键，这里模拟同样的行为
        doAnswer(invocation -> {
            invocation.getArgument(0, SysUser.class).setUserId(9L);
            return null;
        }).when(userRepository).insert(any(SysUser.class));

        SysUser created = userService.create(request, List.of(2L, 3L));

        assertThat(created.getUserId()).isEqualTo(9L);
        assertThat(created.getPassword()).isEqualTo("encoded-password");
        assertThat(created.getStatus()).isEqualTo("0");
        assertThat(created.getDeleted()).isZero();
        assertThat(created.getCreateBy()).isEqualTo("system");
        verify(userRepository).replaceRoles(9L, List.of(2L, 3L));
    }

    @Test
    void getShouldThrowWhenUserMissing() {
        when(userRepository.findById(404L)).thenReturn(null);

        assertThatThrownBy(() -> userService.get(404L))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> assertThat(((BizException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void authByUserNameShouldReturnNullWhenUserMissing() {
        when(userRepository.findByUserName("ghost")).thenReturn(null);

        assertThat(userService.authByUserName("ghost")).isNull();
    }
}
