-- 1. 컬럼 먼저 추가
ALTER TABLE reserves
    ADD COLUMN review_id INT DEFAULT NULL;

-- 2. 외래 키 제약 추가
ALTER TABLE reserves
    ADD CONSTRAINT fk_reserve_review
        FOREIGN KEY (review_id) REFERENCES reviews(id)
            ON DELETE SET NULL;
