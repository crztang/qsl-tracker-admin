package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.domain.SysDict;
import com.qsl.tracker.domain.SysDictItem;
import com.qsl.tracker.mapper.SysDictItemMapper;
import com.qsl.tracker.mapper.SysDictMapper;
import com.qsl.tracker.service.DictService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements DictService {

    private final SysDictMapper sysDictMapper;

    @Override
    public List<SysDictItem> listItems(String dictCode) {
        SysDict dict = sysDictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getCode, dictCode)
                .eq(SysDict::getEnabled, true)
                .last("limit 1"));
        if (dict == null) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, dict.getId())
                .eq(SysDictItem::getEnabled, true)
                .orderByAsc(SysDictItem::getSortOrder, SysDictItem::getId));
    }
}
