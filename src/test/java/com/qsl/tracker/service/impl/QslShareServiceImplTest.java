package com.qsl.tracker.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.qsl.tracker.common.CurrentUserContext;
import com.qsl.tracker.common.ShareTokenUtil;
import com.qsl.tracker.domain.QsoLog;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.domain.QslShare;
import com.qsl.tracker.mapper.QsoLogMapper;
import com.qsl.tracker.mapper.QslShareMapper;
import com.qsl.tracker.mapper.UserMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class QslShareServiceImplTest {

    @BeforeAll
    static void initTableMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), QslShare.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), QsoLog.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), User.class);
    }

    @Test
    void renderHtmlShowsRecentRecordsAndNoScript() {
        QslShareMapper shareMapper = mock(QslShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        QsoLogMapper qsoLogMapper = mock(QsoLogMapper.class);
        CurrentUserContext currentUserContext = mock(CurrentUserContext.class);

        QslShare share = new QslShare();
        share.setId(1L);
        share.setUserId(7L);
        share.setShareTokenHash(ShareTokenUtil.sha256Hex("share-token"));
        share.setEnabled(Boolean.TRUE);
        share.setRecordLimit(2);
        share.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(shareMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(share);
        when(userMapper.selectById(7L)).thenReturn(user());
        when(qsoLogMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                qsoLog("BG1AAA", LocalDateTime.of(2026, 6, 13, 14, 30), "20m", "SSB", "59", "59", "CN"),
                qsoLog("BG1BBB", LocalDateTime.of(2026, 6, 12, 10, 15), "40m", "CW", "599", "599", "SH")
        ));

        QslShareServiceImpl service = new QslShareServiceImpl(currentUserContext, userMapper, qsoLogMapper);
        ReflectionTestUtils.setField(service, "baseMapper", shareMapper);

        String html = service.renderHtml("share-token", null);

        assertThat(html).contains("<!doctype html>");
        assertThat(html).contains("最近通联记录");
        assertThat(html).contains("BG1AAA");
        assertThat(html).contains("BG1BBB");
        assertThat(html).doesNotContain("<script");
    }

    @Test
    void currentSummaryUsesDefaultsWhenMissing() {
        QslShareMapper shareMapper = mock(QslShareMapper.class);
        CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
        when(currentUserContext.userId()).thenReturn(7L);
        when(shareMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

        QslShareServiceImpl service = new QslShareServiceImpl(
                currentUserContext, mock(UserMapper.class), mock(QsoLogMapper.class));
        ReflectionTestUtils.setField(service, "baseMapper", shareMapper);

        assertThat(service.current().getRecordLimit()).isEqualTo(10);
        assertThat(service.current().getEnabled()).isFalse();
        assertThat(service.current().getExpiryPreset()).isEqualTo("permanent");
    }

    private User user() {
        User user = new User();
        user.setId(7L);
        user.setUsername("demo");
        user.setCallSign("BG1XYZ");
        user.setEnabled(Boolean.TRUE);
        return user;
    }

    private QsoLog qsoLog(String callSign, LocalDateTime time, String bd, String mode, String rstSent,
            String rstReceived, String qth) {
        QsoLog log = new QsoLog();
        log.setCallSign(callSign);
        log.setQsoTime(time);
        log.setFrequencyMhz(new BigDecimal("14.074"));
        log.setBd(bd);
        log.setMode(mode);
        log.setRstSent(rstSent);
        log.setRstReceived(rstReceived);
        log.setQth(qth);
        return log;
    }
}
