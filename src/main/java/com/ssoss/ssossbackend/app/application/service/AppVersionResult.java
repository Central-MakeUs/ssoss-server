package com.ssoss.ssossbackend.app.application.service;

public record AppVersionResult(boolean updateRequired, String minimumVersion) {
}
