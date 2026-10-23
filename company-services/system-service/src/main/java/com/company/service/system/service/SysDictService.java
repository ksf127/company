package com.company.service.system.service;

import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysDictData;
import com.company.service.system.entity.SysDictType;
import com.company.service.system.repository.SysDictRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictService {

    private final SysDictRepository dictRepository;

    public PageResult<SysDictType> pageTypes(int pageNum, int pageSize, String dictName, String dictType) {
        return dictRepository.pageTypes(pageNum, pageSize, dictName, dictType);
    }

    public List<SysDictType> listTypes() {
        return dictRepository.findAllTypes();
    }

    @Transactional
    public SysDictType createType(SysDictType type) {
        type.setDictId(null);
        type.setStatus(StringUtils.hasText(type.getStatus()) ? type.getStatus() : "0");
        type.setCreateTime(LocalDateTime.now());
        dictRepository.insertType(type);
        return type;
    }

    @Transactional
    public void updateType(SysDictType type) {
        if (dictRepository.findTypeById(type.getDictId()) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "字典类型不存在: " + type.getDictId());
        }
        dictRepository.updateType(type);
    }

    @Transactional
    public void deleteTypes(Long[] dictIds) {
        if (dictIds != null && dictIds.length > 0) {
            dictRepository.deleteTypes(dictIds);
        }
    }

    public PageResult<SysDictData> pageData(int pageNum, int pageSize, String dictType) {
        return dictRepository.pageData(pageNum, pageSize, dictType);
    }

    public List<SysDictData> dataByType(String dictType) {
        return dictRepository.findDataByType(dictType);
    }

    @Transactional
    public SysDictData createData(SysDictData data) {
        data.setDictCode(null);
        data.setStatus(StringUtils.hasText(data.getStatus()) ? data.getStatus() : "0");
        data.setCreateTime(LocalDateTime.now());
        dictRepository.insertData(data);
        return data;
    }

    @Transactional
    public void updateData(SysDictData data) {
        if (dictRepository.findDataById(data.getDictCode()) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "字典数据不存在: " + data.getDictCode());
        }
        dictRepository.updateData(data);
    }

    @Transactional
    public void deleteData(Long[] dictCodes) {
        if (dictCodes != null && dictCodes.length > 0) {
            dictRepository.deleteData(dictCodes);
        }
    }
}
