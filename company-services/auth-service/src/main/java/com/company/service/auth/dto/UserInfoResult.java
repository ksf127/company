package com.company.service.auth.dto;

import com.company.api.system.SysRoleDTO;
import com.company.api.system.SysUserDTO;

import java.util.List;

public record UserInfoResult(SysUserDTO user, List<SysRoleDTO> roles, List<String> permissions) {
}
