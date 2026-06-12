package com.qsl.tracker.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.CurrentUserContext;
import com.qsl.tracker.domain.QsoLog;
import com.qsl.tracker.dto.PrintTemplateRequest;
import com.qsl.tracker.mapper.PrintTemplateMapper;
import com.qsl.tracker.mapper.QsoLogMapper;
import com.qsl.tracker.service.DictService;
import com.qsl.tracker.service.DataScopeService;
import com.qsl.tracker.service.QslCardService;
import com.qsl.tracker.service.StorageFileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class OwnershipScopeTest {

    @BeforeAll
    static void initializeTableMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                QsoLog.class);
    }

    @Test
    void normalUserDetailQueryContainsOwnerCondition() {
        QsoLogMapper mapper = mock(QsoLogMapper.class);
        CurrentUserContext currentUser = mock(CurrentUserContext.class);
        DataScopeService dataScope = mock(DataScopeService.class);
        when(currentUser.userId()).thenReturn(7L);
        when(dataScope.canAccessAll()).thenReturn(false);
        when(mapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

        QsoLogServiceImpl service = new QsoLogServiceImpl(
                mock(QslCardService.class), mock(DictService.class), currentUser, dataScope);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        assertThatThrownBy(() -> service.detail(99L)).isInstanceOf(BusinessException.class);

        ArgumentCaptor<Wrapper<QsoLog>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectOne(captor.capture(), anyBoolean());
        assertThat(captor.getValue().getSqlSegment()).contains("user_id");
    }

    @Test
    void allDataScopeDetailQueryDoesNotRestrictOwner() {
        QsoLogMapper mapper = mock(QsoLogMapper.class);
        CurrentUserContext currentUser = mock(CurrentUserContext.class);
        DataScopeService dataScope = mock(DataScopeService.class);
        when(dataScope.canAccessAll()).thenReturn(true);
        when(mapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

        QsoLogServiceImpl service = new QsoLogServiceImpl(
                mock(QslCardService.class), mock(DictService.class), currentUser, dataScope);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        assertThatThrownBy(() -> service.detail(99L)).isInstanceOf(BusinessException.class);

        ArgumentCaptor<Wrapper<QsoLog>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectOne(captor.capture(), anyBoolean());
        assertThat(captor.getValue().getSqlSegment()).doesNotContain("user_id");
    }

    @Test
    void templateBackgroundIsResolvedAgainstCurrentOwner() {
        StorageFileService storageFileService = mock(StorageFileService.class);
        CurrentUserContext currentUser = mock(CurrentUserContext.class);
        when(currentUser.userId()).thenReturn(7L);
        when(storageFileService.getFileForOwner("foreign-key", 7L))
                .thenThrow(new BusinessException("文件不存在"));

        PrintTemplateServiceImpl service = new PrintTemplateServiceImpl(
                storageFileService,
                new ObjectMapper(),
                currentUser,
                mock(DataScopeService.class));
        ReflectionTestUtils.setField(service, "baseMapper", mock(PrintTemplateMapper.class));

        PrintTemplateRequest request = new PrintTemplateRequest();
        request.setTemplateName("test");
        request.setTemplateType("1");
        request.setBackgroundFileKey("foreign-key");
        request.setConfigJson(new ObjectMapper().createObjectNode());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文件不存在");
        verify(storageFileService).getFileForOwner("foreign-key", 7L);
    }
}
