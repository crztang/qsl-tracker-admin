package com.qsl.tracker.config;

import static org.assertj.core.api.Assertions.assertThat;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

class SaTokenJwtConfigTest {

    @Test
    void usesJwtSimpleLogic() {
        SaTokenConfig config = new SaTokenConfig(null);

        assertThat(config.stpLogicJwt()).isInstanceOf(StpLogicJwtForSimple.class);
    }

    @Test
    void devConfigUsesRedisAndSingleLogin() {
        Properties properties = load("application-dev.yml");

        assertThat(properties.getProperty("spring.data.redis.host")).isEqualTo("192.168.100.128");
        assertThat(properties.getProperty("spring.data.redis.port")).isEqualTo("6379");
        assertThat(properties.getProperty("sa-token.is-concurrent")).isEqualTo("false");
        assertThat(properties.getProperty("sa-token.is-share")).isEqualTo("false");
        assertThat(properties.getProperty("sa-token.timeout")).isEqualTo("259200");
        assertThat(properties.getProperty("sa-token.jwt-secret-key"))
                .startsWith("${SA_TOKEN_JWT_SECRET_KEY:");
    }

    @Test
    void prodConfigRequiresExternalRedisAndJwtSecret() {
        Properties properties = load("application-prod.yml");

        assertThat(properties.getProperty("spring.data.redis.host")).isEqualTo("${REDIS_HOST}");
        assertThat(properties.getProperty("spring.data.redis.password")).isEqualTo("${REDIS_PASSWORD:}");
        assertThat(properties.getProperty("sa-token.jwt-secret-key")).isEqualTo("${SA_TOKEN_JWT_SECRET_KEY}");
    }

    private Properties load(String resource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource(resource));
        return factory.getObject();
    }
}
