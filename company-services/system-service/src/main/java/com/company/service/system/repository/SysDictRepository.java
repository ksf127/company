package com.company.service.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysDictData;
import com.company.service.system.entity.SysDictType;
import com.company.service.system.mapper.SysDictDataMapper;
import com.company.service.system.mapper.SysDictTypeMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SysDictRepository {

    private final SysDictTypeMapper typeMapper;

    private final SysDictDataMapper dataMapper;

    public PageResult<SysDictType> pageTypes(int pageNum, int pageSize, String dictName, String dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .like(StringUtils.hasText(dictName), SysDictType::getDictName, dictName)
                .like(StringUtils.hasText(dictType), SysDictType::getDictType, dictType)
                .orderByAsc(SysDictType::getDictId);
        Page<SysDictType> page = typeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public List<SysDictType> findAllTypes() {
        return typeMapper.selectList(new LambdaQueryWrapper<SysDictType>().orderByAsc(SysDictType::getDictId));
    }

    public SysDictType findTypeById(Long dictId) {
        return typeMapper.selectById(dictId);
    }

    public void insertType(SysDictType type) {
        typeMapper.insert(type);
    }

    public void updateType(SysDictType type) {
        typeMapper.updateById(type);
    }

    public void deleteTypes(Long[] dictIds) {
        typeMapper.deleteBatchIds(Arrays.asList(dictIds));
    }

    public PageResult<SysDictData> pageData(int pageNum, int pageSize, String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<SysDictData>()
                .eq(StringUtils.hasText(dictType), SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort);
        Page<SysDictData> page = dataMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public List<SysDictData> findDataByType(String dictType) {
        return dataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort));
    }

    public SysDictData findDataById(Long dictCode) {
        return dataMapper.selectById(dictCode);
    }

    public void insertData(SysDictData data) {
        dataMapper.insert(data);
    }

    public void updateData(SysDictData data) {
        dataMapper.updateById(data);
    }

    public void deleteData(Long[] dictCodes) {
        dataMapper.deleteBatchIds(Arrays.asList(dictCodes));
    }
}
