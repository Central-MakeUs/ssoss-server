package com.ssoss.ssossbackend.auth.domain.contract;

public interface TokenHasher {

    String hash(String token);
}
