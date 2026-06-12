package com.qsl.tracker.service.impl;

import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.config.CaptchaProperties;
import com.qsl.tracker.dto.CaptchaResponse;
import com.qsl.tracker.service.CaptchaService;
import com.pig4cloud.captcha.ArithmeticCaptcha;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final Set<String> SCENES = Set.of("login", "register");
    private static final DefaultRedisScript<String> CONSUME_SCRIPT = new DefaultRedisScript<>(
            "local value = redis.call('GET', KEYS[1]); "
                    + "if value then redis.call('DEL', KEYS[1]); end; "
                    + "return value;",
            String.class);
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]); "
                    + "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; "
                    + "return count;",
            Long.class);

    private final StringRedisTemplate redisTemplate;
    private final CaptchaProperties properties;

    @Override
    public CaptchaResponse generate(String scene, String ip) {
        String normalizedScene = normalizeScene(scene);
        checkRateLimit("generate", normalizedScene, ip, properties.getGenerateLimit());
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(
                properties.getWidth(), properties.getHeight());
        captcha.setLen(2);
        captcha.setDifficulty(properties.getDifficulty());
        captcha.supportAlgorithmSign(3);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
                captchaKey(normalizedScene, captchaId),
                captcha.text(),
                Duration.ofSeconds(properties.getTtlSeconds()));
        return new CaptchaResponse(captchaId, captcha.toBase64(), properties.getTtlSeconds());
    }

    @Override
    public void verify(String scene, String captchaId, String captchaCode, String ip) {
        String normalizedScene = normalizeScene(scene);
        checkRateLimit("verify", normalizedScene, ip, properties.getVerifyLimit());
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException("请输入验证码");
        }
        String expected = redisTemplate.execute(
                CONSUME_SCRIPT, List.of(captchaKey(normalizedScene, captchaId.trim())));
        if (expected == null || !expected.equals(captchaCode.trim())) {
            throw new BusinessException("验证码错误或已过期");
        }
    }

    private void checkRateLimit(String action, String scene, String ip, int limit) {
        String safeIp = ip == null || ip.isBlank() ? "unknown" : ip;
        Long count = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of("qsl:captcha:rate:" + action + ":" + scene + ":" + safeIp),
                String.valueOf(properties.getRateWindowSeconds()));
        if (count == null || count > limit) {
            throw new BusinessException(429, "操作过于频繁，请稍后再试");
        }
    }

    private String normalizeScene(String scene) {
        String value = scene == null ? "" : scene.trim().toLowerCase(Locale.ROOT);
        if (!SCENES.contains(value)) {
            throw new BusinessException("验证码场景不正确");
        }
        return value;
    }

    private String captchaKey(String scene, String captchaId) {
        return "qsl:captcha:" + scene + ":" + captchaId;
    }
}
