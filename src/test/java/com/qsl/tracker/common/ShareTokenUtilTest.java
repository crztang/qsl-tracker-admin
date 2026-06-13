package com.qsl.tracker.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShareTokenUtilTest {

    @Test
    void generatesUrlSafeToken() {
        String token = ShareTokenUtil.newToken();

        assertThat(token).hasSize(64);
        assertThat(token).matches("^[A-Za-z0-9]+$");
    }

    @Test
    void hashesToStableSha256Hex() {
        assertThat(ShareTokenUtil.sha256Hex("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
