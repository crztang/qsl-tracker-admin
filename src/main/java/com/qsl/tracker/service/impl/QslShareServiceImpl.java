package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.CurrentUserContext;
import com.qsl.tracker.common.ShareTokenUtil;
import com.qsl.tracker.domain.QsoLog;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.domain.QslShare;
import com.qsl.tracker.dto.QslShareIssueResponse;
import com.qsl.tracker.dto.QslShareRequest;
import com.qsl.tracker.dto.QslShareSummaryResponse;
import com.qsl.tracker.mapper.QsoLogMapper;
import com.qsl.tracker.mapper.QslShareMapper;
import com.qsl.tracker.mapper.UserMapper;
import com.qsl.tracker.service.QslShareService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class QslShareServiceImpl extends ServiceImpl<QslShareMapper, QslShare>
        implements QslShareService {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CurrentUserContext currentUserContext;
    private final UserMapper userMapper;
    private final QsoLogMapper qsoLogMapper;

    @Override
    public QslShareSummaryResponse current() {
        return toSummary(loadCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QslShareIssueResponse generate(QslShareRequest request, HttpServletRequest httpRequest) {
        QslShare entity = upsertCurrent(request, true);
        String token = ShareTokenUtil.newToken();
        entity.setShareTokenHash(ShareTokenUtil.sha256Hex(token));
        saveOrUpdate(entity);
        return toIssueResponse(entity, token, httpRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QslShareSummaryResponse updateSettings(QslShareRequest request) {
        QslShare entity = upsertCurrent(request, false);
        saveOrUpdate(entity);
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QslShareSummaryResponse revoke() {
        QslShare entity = loadOrCreateCurrent();
        entity.setEnabled(Boolean.FALSE);
        saveOrUpdate(entity);
        return toSummary(entity);
    }

    @Override
    public String renderHtml(String token, HttpServletRequest request) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(404, "嵌入链接不存在");
        }
        String tokenHash = ShareTokenUtil.sha256Hex(token);
        QslShare share = getOne(new LambdaQueryWrapper<QslShare>()
                .eq(QslShare::getShareTokenHash, tokenHash)
                .eq(QslShare::getEnabled, Boolean.TRUE)
                .last("limit 1"));
        if (share == null) {
            throw new BusinessException(404, "嵌入链接不存在或已失效");
        }
        if (share.getExpiresAt() != null && share.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(404, "嵌入链接已过期");
        }
        User user = userMapper.selectById(share.getUserId());
        if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
            throw new BusinessException(404, "嵌入链接不存在或已失效");
        }
        List<QsoLog> logs = qsoLogMapper.selectList(new LambdaQueryWrapper<QsoLog>()
                .eq(QsoLog::getUserId, share.getUserId())
                .orderByDesc(QsoLog::getQsoTime)
                .last("limit " + limitValue(share.getRecordLimit())));
        return buildHtml(user, share, logs);
    }

    private QslShare upsertCurrent(QslShareRequest request, boolean forceEnabled) {
        QslShare entity = loadOrCreateCurrent();
        entity.setRecordLimit(limitValue(request.getRecordLimit()));
        entity.setExpiresAt(resolveExpiresAt(request.getExpiryPreset()));
        entity.setEnabled(forceEnabled || Boolean.TRUE.equals(request.getEnabled()));
        return entity;
    }

    private QslShare loadCurrent() {
        QslShare entity = getOne(new LambdaQueryWrapper<QslShare>()
                .eq(QslShare::getUserId, currentUserContext.userId())
                .last("limit 1"));
        if (entity != null) {
            return entity;
        }
        QslShare empty = new QslShare();
        empty.setUserId(currentUserContext.userId());
        empty.setRecordLimit(10);
        empty.setEnabled(Boolean.FALSE);
        return empty;
    }

    private QslShare loadOrCreateCurrent() {
        QslShare entity = getOne(new LambdaQueryWrapper<QslShare>()
                .eq(QslShare::getUserId, currentUserContext.userId())
                .last("limit 1"));
        if (entity != null) {
            return entity;
        }
        QslShare empty = new QslShare();
        empty.setUserId(currentUserContext.userId());
        empty.setRecordLimit(10);
        empty.setEnabled(Boolean.FALSE);
        return empty;
    }

    private QslShareSummaryResponse toSummary(QslShare entity) {
        QslShareSummaryResponse response = new QslShareSummaryResponse();
        response.setEnabled(Boolean.TRUE.equals(entity.getEnabled()));
        response.setRecordLimit(limitValue(entity.getRecordLimit()));
        response.setExpiresAt(entity.getExpiresAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setExpiryPreset(resolveExpiryPreset(entity.getExpiresAt()));
        response.setExpired(entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(LocalDateTime.now()));
        response.setHasToken(hasToken(entity));
        return response;
    }

    private QslShareIssueResponse toIssueResponse(QslShare entity, String token, HttpServletRequest httpRequest) {
        QslShareIssueResponse response = new QslShareIssueResponse();
        copySummary(entity, response);
        response.setToken(token);
        response.setEmbedUrl(buildEmbedUrl(token, httpRequest));
        response.setIframeCode(buildIframeCode(response.getEmbedUrl(), response.getRecordLimit()));
        return response;
    }

    private void copySummary(QslShare entity, QslShareSummaryResponse response) {
        response.setEnabled(Boolean.TRUE.equals(entity.getEnabled()));
        response.setRecordLimit(limitValue(entity.getRecordLimit()));
        response.setExpiresAt(entity.getExpiresAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setExpiryPreset(resolveExpiryPreset(entity.getExpiresAt()));
        response.setExpired(entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(LocalDateTime.now()));
        response.setHasToken(hasToken(entity));
    }

    private String buildEmbedUrl(String token, HttpServletRequest httpRequest) {
        String baseUrl = currentPublicBaseUrl(httpRequest);
        return baseUrl + "/public/embed/" + token;
    }

    private String buildIframeCode(String embedUrl, Integer recordLimit) {
        int height = 560;
        return "<iframe src=\"" + HtmlUtils.htmlEscape(embedUrl) + "\""
                + " width=\"100%\" height=\"" + height + "\""
                + " loading=\"lazy\" referrerpolicy=\"no-referrer\""
                + " style=\"border:0;display:block;width:100%;overflow:hidden;\""
                + " title=\"最近通联记录\"></iframe>";
    }

    private String buildHtml(User user, QslShare share, List<QsoLog> logs) {
        StringBuilder html = new StringBuilder(4096);
        String title = displayName(user) + " 最近通联记录";
        html.append("<!doctype html><html lang=\"zh-CN\"><head>")
                .append("<meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<meta name=\"referrer\" content=\"no-referrer\">")
                .append("<meta name=\"robots\" content=\"noindex,nofollow\">")
                .append("<title>").append(escape(title)).append("</title>")
                .append("<style>")
                .append(":root{color-scheme:light;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Arial,sans-serif;color:#111827;background:transparent;}")
                .append("*{box-sizing:border-box;}body{margin:0;padding:0;background:transparent;}")
                .append(".embed-card{width:100%;border:1px solid #e5e7eb;border-radius:14px;background:#fff;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.08);}")
                .append(".embed-header{display:flex;flex-wrap:wrap;gap:8px 12px;justify-content:space-between;align-items:center;padding:16px 18px;border-bottom:1px solid #eef2f7;background:linear-gradient(135deg,#f8fafc 0%,#ffffff 100%);}")
                .append(".embed-header h1{margin:0;font-size:18px;line-height:1.25;}")
                .append(".meta{color:#6b7280;font-size:12px;line-height:1.4;text-align:right;}")
                .append(".table-wrap{max-height:420px;overflow:auto;}")
                .append("table{width:100%;border-collapse:collapse;}")
                .append("thead th{position:sticky;top:0;z-index:1;background:#f9fafb;}")
                .append("th,td{padding:12px 14px;border-bottom:1px solid #eef2f7;text-align:left;vertical-align:top;font-size:13px;line-height:1.4;white-space:nowrap;}")
                .append("td:last-child,th:last-child{white-space:normal;}")
                .append(".empty{padding:28px 16px;color:#6b7280;text-align:center;}")
                .append(".footer{display:flex;justify-content:space-between;gap:12px;padding:12px 18px 16px;color:#6b7280;font-size:12px;}")
                .append("@media (max-width:640px){.embed-header,.footer{flex-direction:column;align-items:flex-start}.meta{text-align:left}th,td{font-size:12px;padding:10px 12px;}}")
                .append("</style></head><body>")
                .append("<section class=\"embed-card\">")
                .append("<header class=\"embed-header\">")
                .append("<div><h1>").append(escape(title)).append("</h1>")
                .append("<div class=\"meta\">").append(escape("更新于 " + LocalDateTime.now().format(DISPLAY_TIME))).append("</div></div>")
                .append("<div class=\"meta\">").append(escape("仅供 iframe 嵌入")).append("</div>")
                .append("</header>")
                .append("<div class=\"table-wrap\"><table><thead><tr>")
                .append("<th>时间</th><th>呼号</th><th>频率 / 波段</th><th>模式</th><th>RST</th><th>QTH</th>")
                .append("</tr></thead><tbody>");

        if (logs.isEmpty()) {
            html.append("<tr><td colspan=\"6\" class=\"empty\">暂无通联记录</td></tr>");
        } else {
            for (QsoLog log : logs) {
                html.append("<tr>")
                        .append("<td>").append(escape(displayTime(log.getQsoTime()))).append("</td>")
                        .append("<td>").append(escape(value(log.getCallSign()))).append("</td>")
                        .append("<td>").append(escape(frequencyText(log))).append("</td>")
                        .append("<td>").append(escape(value(log.getMode()))).append("</td>")
                        .append("<td>").append(escape(rstText(log))).append("</td>")
                        .append("<td>").append(escape(value(log.getQth()))).append("</td>")
                        .append("</tr>");
            }
        }

        html.append("</tbody></table></div>")
                .append("<footer class=\"footer\">")
                .append("<span>").append(escape("有效期：" + expiryText(share))).append("</span>")
                .append("<span>").append(escape("仅展示最近 " + limitValue(share.getRecordLimit()) + " 条记录")).append("</span>")
                .append("</footer>")
                .append("</section></body></html>");
        return html.toString();
    }

    private String expiryText(QslShare share) {
        if (share.getExpiresAt() == null) {
            return "永久有效";
        }
        return share.getExpiresAt().format(DISPLAY_TIME);
    }

    private String displayName(User user) {
        if (user.getCallSign() != null && !user.getCallSign().isBlank()) {
            return user.getCallSign().trim().toUpperCase(Locale.ROOT);
        }
        return user.getUsername();
    }

    private String displayTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DISPLAY_TIME);
    }

    private String frequencyText(QsoLog log) {
        String frequency = log.getFrequencyMhz() == null
                ? null
                : log.getFrequencyMhz().stripTrailingZeros().toPlainString() + " MHz";
        String band = value(log.getBd());
        if (frequency != null && !"-".equals(band)) {
            return frequency + " / " + band;
        }
        return frequency != null ? frequency : band;
    }

    private String rstText(QsoLog log) {
        String sent = value(log.getRstSent());
        String received = value(log.getRstReceived());
        if ("-".equals(sent) && "-".equals(received)) {
            return "-";
        }
        return sent + " / " + received;
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }

    private String escape(String text) {
        return HtmlUtils.htmlEscape(text == null ? "" : text);
    }

    private boolean hasToken(QslShare entity) {
        return entity.getShareTokenHash() != null && !entity.getShareTokenHash().isBlank();
    }

    private int limitValue(Integer value) {
        return Math.max(1, Math.min(50, Objects.requireNonNullElse(value, 10)));
    }

    private LocalDateTime resolveExpiresAt(String expiryPreset) {
        if (expiryPreset == null || expiryPreset.isBlank() || "permanent".equalsIgnoreCase(expiryPreset)) {
            return null;
        }
        return switch (expiryPreset) {
            case "1d" -> LocalDateTime.now().plusDays(1);
            case "7d" -> LocalDateTime.now().plusDays(7);
            case "30d" -> LocalDateTime.now().plusDays(30);
            default -> throw new BusinessException("无效的有效期设置");
        };
    }

    private String resolveExpiryPreset(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return "permanent";
        }
        long hours = Duration.between(LocalDateTime.now(), expiresAt).toHours();
        if (Math.abs(hours - 24) <= 12) {
            return "1d";
        }
        if (Math.abs(hours - 168) <= 18) {
            return "7d";
        }
        if (Math.abs(hours - 720) <= 24) {
            return "30d";
        }
        return "custom";
    }

    private String currentPublicBaseUrl(HttpServletRequest httpRequest) {
        if (httpRequest == null) {
            return "";
        }
        return ServletUriComponentsBuilder.fromRequest(httpRequest)
                .replacePath("")
                .replaceQuery(null)
                .build()
                .toUriString();
    }
}
