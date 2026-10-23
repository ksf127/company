package com.company.service.system.service;

import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysLoginLog;
import com.company.service.system.repository.SysLoginLogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysLoginLogService {

    private final SysLoginLogRepository logRepository;

    public PageResult<SysLoginLog> page(int pageNum, int pageSize, String userName, String status) {
        return logRepository.page(pageNum, pageSize, userName, status);
    }

    /** 审计写入失败不允许影响登录主流程，因此调用方负责吞掉异常 */
    public void record(String userName, String ipaddr, String browser, String os, String status, String msg) {
        SysLoginLog log = new SysLoginLog();
        log.setUserName(userName);
        log.setIpaddr(ipaddr);
        log.setBrowser(browser);
        log.setOs(os);
        log.setStatus(status);
        log.setMsg(msg);
        log.setLoginTime(LocalDateTime.now());
        logRepository.insert(log);
    }
}
