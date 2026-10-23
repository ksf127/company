package com.company.service.demo06.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveRecordRequest(
        @NotBlank(message = "名称不能为空") String name,
        @NotBlank(message = "负责人不能为空") String owner,
        String status,
        String remark) {

    public SaveRecordRequest {
        status = (status == null || status.isBlank()) ? "0" : status;
    }
}
