package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.CurrentUserContext;
import com.qsl.tracker.common.ShareTokenCrypto;
import com.qsl.tracker.common.ShareTokenUtil;
import com.qsl.tracker.domain.QslShare;
import com.qsl.tracker.domain.QsoLog;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.dto.QslShareIssueResponse;
import com.qsl.tracker.dto.QslShareRequest;
import com.qsl.tracker.dto.QslShareSummaryResponse;
import com.qsl.tracker.mapper.QslShareMapper;
import com.qsl.tracker.mapper.QsoLogMapper;
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

@Service
@RequiredArgsConstructor
public class QslShareServiceImpl extends ServiceImpl<QslShareMapper, QslShare>
        implements QslShareService {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CurrentUserContext currentUserContext;
    private final UserMapper userMapper;
    private final QsoLogMapper qsoLogMapper;

    @Value("${qsl.public-base-url}")
    private String publicBaseUrl;

    @Value("${sa-token.jwt-secret-key}")
    private String shareTokenSecret;

    @Override
    public QslShareSummaryResponse current() {
        QslShare entity = loadCurrent();
        QslShareSummaryResponse response = toSummary(entity);
        hydrateShareLinks(entity, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QslShareIssueResponse generate(QslShareRequest request, HttpServletRequest httpRequest) {
        QslShare entity = upsertCurrent(request);
        String token = ShareTokenUtil.newToken();
        entity.setShareTokenHash(ShareTokenUtil.sha256Hex(token));
        entity.setShareTokenCiphertext(ShareTokenCrypto.encrypt(token, shareTokenSecret));
        saveOrUpdate(entity);
        return toIssueResponse(entity, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QslShareSummaryResponse updateSettings(QslShareRequest request) {
        QslShare entity = upsertCurrent(request);
        saveOrUpdate(entity);
        QslShareSummaryResponse response = toSummary(entity);
        hydrateShareLinks(entity, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QslShareSummaryResponse revoke() {
        QslShare entity = loadOrCreateCurrent();
        if (entity.getId() != null) {
            removeById(entity.getId());
        }
        QslShareSummaryResponse response = new QslShareSummaryResponse();
        response.setEnabled(Boolean.FALSE);
        response.setRecordLimit(10);
        response.setExpiryPreset("permanent");
        response.setExpired(false);
        response.setHasToken(false);
        response.setToken(null);
        response.setEmbedUrl(null);
        response.setIframeCode(null);
        return response;
    }

    @Override
    public String renderHtml(String token, HttpServletRequest request) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(404, "页面不存在");
        }
        String tokenHash = ShareTokenUtil.sha256Hex(token);
        QslShare share = getOne(new LambdaQueryWrapper<QslShare>()
                .eq(QslShare::getShareTokenHash, tokenHash)
                .eq(QslShare::getEnabled, Boolean.TRUE)
                .last("limit 1"));
        if (share == null || isExpired(share.getExpiresAt())) {
            throw new BusinessException(404, "页面不存在");
        }
        User user = userMapper.selectById(share.getUserId());
        if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
            throw new BusinessException(404, "页面不存在");
        }
        List<QsoLog> logs = qsoLogMapper.selectList(new LambdaQueryWrapper<QsoLog>()
                .eq(QsoLog::getUserId, share.getUserId())
                .orderByDesc(QsoLog::getQsoTime)
                .last("limit " + limitValue(share.getRecordLimit())));
        return buildHtml(user, share, logs);
    }

    private QslShare upsertCurrent(QslShareRequest request) {
        QslShare entity = loadOrCreateCurrent();
        entity.setRecordLimit(limitValue(request.getRecordLimit()));
        entity.setExpiresAt(resolveExpiresAt(request.getExpiryPreset()));
        entity.setEnabled(Boolean.TRUE);
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
        empty.setEnabled(Boolean.TRUE);
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
        response.setExpired(isExpired(entity.getExpiresAt()));
        response.setHasToken(hasToken(entity));
        return response;
    }

    private QslShareIssueResponse toIssueResponse(QslShare entity, String token) {
        QslShareIssueResponse response = new QslShareIssueResponse();
        copySummary(entity, response);
        response.setToken(token);
        response.setEmbedUrl(buildEmbedUrl(token));
        response.setIframeCode(buildIframeCode(response.getEmbedUrl()));
        return response;
    }

    private void hydrateShareLinks(QslShare entity, QslShareSummaryResponse response) {
        String token = resolveToken(entity);
        if (token == null || token.isBlank()) {
            response.setToken(null);
            response.setEmbedUrl(null);
            response.setIframeCode(null);
            return;
        }
        response.setToken(token);
        response.setEmbedUrl(buildEmbedUrl(token));
        response.setIframeCode(buildIframeCode(response.getEmbedUrl()));
    }

    private void copySummary(QslShare entity, QslShareSummaryResponse response) {
        response.setEnabled(Boolean.TRUE.equals(entity.getEnabled()));
        response.setRecordLimit(limitValue(entity.getRecordLimit()));
        response.setExpiresAt(entity.getExpiresAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setExpiryPreset(resolveExpiryPreset(entity.getExpiresAt()));
        response.setExpired(isExpired(entity.getExpiresAt()));
        response.setHasToken(hasToken(entity));
    }

    private String buildEmbedUrl(String token) {
        return normalizePublicBaseUrl() + "/public/embed/" + token;
    }

    private String buildIframeCode(String embedUrl) {
        int height = 560;
        return "<iframe src=\"" + HtmlUtils.htmlEscape(embedUrl) + "\""
                + " width=\"100%\" height=\"" + height + "\""
                + " loading=\"lazy\" referrerpolicy=\"no-referrer\""
                + " style=\"border:0;display:block;width:100%;overflow:hidden;\""
                + " title=\"最近通联记录\"></iframe>";
    }

    private String buildHtml(User user, QslShare share, List<QsoLog> logs) {
        StringBuilder html = new StringBuilder(4096);
        String title = displayTitle(user);
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
                .append("@media (max-width:640px){.embed-header{flex-direction:column;align-items:flex-start}.meta{text-align:left}th,td{font-size:12px;padding:10px 12px;}}")
                .append("</style></head><body>")
                .append("<section class=\"embed-card\">")
                .append("<header class=\"embed-header\">")
                .append("<div><h1>").append(escape(title)).append("</h1>")
                .append("<div class=\"meta\">").append(escape("更新于 " + LocalDateTime.now().format(DISPLAY_TIME))).append("</div></div>")
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
                .append("</section></body></html>");
        return html.toString();
    }

    private String displayTitle(User user) {
        String callSign = user.getCallSign();
        if (callSign != null && !callSign.isBlank()) {
            return callSign.trim().toUpperCase(Locale.ROOT) + " 最近通联记录";
        }
        return "最近通联记录";
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
        return entity.getShareTokenCiphertext() != null && !entity.getShareTokenCiphertext().isBlank();
    }

    private String resolveToken(QslShare entity) {
        if (!hasToken(entity)) {
            return null;
        }
        try {
            return ShareTokenCrypto.decrypt(entity.getShareTokenCiphertext(), shareTokenSecret);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
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

    private String normalizePublicBaseUrl() {
        String baseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
