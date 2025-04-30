ALTER TABLE comments
    ADD COLUMN parent_id INTEGER DEFAULT NULL,
    ADD COLUMN ref_comment_id INTEGER DEFAULT NULL,
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE comments
    ADD CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments(id),
    ADD CONSTRAINT fk_comment_ref FOREIGN KEY (ref_comment_id) REFERENCES comments(id);
