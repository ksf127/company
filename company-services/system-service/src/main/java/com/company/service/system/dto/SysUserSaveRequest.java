package com.company.service.system.dto;

import com.company.service.system.entity.SysUser;

import java.util.List;

/**
 * 用户新增/修改入参：字段扁平化（与若依前端一致），并附带角色关联。
 * 不接受 createdTime/updateTime 等审计字段，避免前端覆盖。
 */
public record SysUserSaveRequest(
        Long userId,
        Long deptId,
        String userName,
        String nickName,
        String userType,
        String email,
        String phonenumber,
        String sex,
        String avatar,
        String password,
        String status,
        String remark,
        List<Long> roleIds) {

    public SysUser toEntity() {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(deptId);
        user.setUserName(userName);
        user.setNickName(nickName);
        user.setUserType(userType);
        user.setEmail(email);
        user.setPhonenumber(phonenumber);
        user.setSex(sex);
        user.setAvatar(avatar);
        user.setPassword(password);
        user.setStatus(status);
        user.setRemark(remark);
        return user;
    }
}
