package com.ssoss.ssossbackend.auth.infrastructure.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

import org.springframework.stereotype.Component;

@Component
class Sha256TokenHasher implements TokenHasher {

    @Override
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException unreachable) {
            throw new BusinessException(CommonErrorCode.INTERNAL_ERROR, unreachable);
        }
    }
}
