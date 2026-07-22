package com.ssoss.ssossbackend.credit.entrypoint.scheduler;

import com.ssoss.ssossbackend.credit.application.service.CreditCycleRenewalResult;
import com.ssoss.ssossbackend.credit.application.service.CreditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditCycleScheduler {

    private final CreditService creditService;

    @Scheduled(cron = "0 0 0 3 * *", zone = "Asia/Seoul")
    public void renewCycles() {
        CreditCycleRenewalResult result = creditService.renewCycles();
        log.info("크레딧 사이클 갱신 배치 완료: 대상 {}명, 지급 {}명", result.targetCount(), result.renewedCount());
    }
}
