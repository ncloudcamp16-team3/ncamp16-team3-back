CREATE TABLE IF NOT EXISTS review_photos
(
    file_id     INTEGER NOT NULL, -- 파일아이디
    review_id INTEGER NOT NULL, -- 리뷰 아이디
    PRIMARY KEY (file_id, review_id),
    FOREIGN KEY (review_id) REFERENCES reviews (id),
    FOREIGN KEY (file_id) REFERENCES files (id)
);