ALTER TABLE credit_ledger
    DROP INDEX uk_credit_ledger_generation_result_id;
ALTER TABLE credit_ledger
    CHANGE generation_result_id generation_id BIGINT NULL COMMENT '차감을 일으킨 생성 작업 id (차감 행만)';
ALTER TABLE credit_ledger
    ADD CONSTRAINT uk_credit_ledger_generation_id UNIQUE (generation_id);
