create schema tailfriends;
use tailfriends;

-- sns타입
CREATE TABLE IF NOT EXISTS sns_types
(
    id   INTEGER     NOT NULL AUTO_INCREMENT, -- 타입아이디
    name VARCHAR(50) NOT NULL,                -- sns이름
    PRIMARY KEY (id)
);
INSERT INTO sns_types (name) VALUES ('KAKAO');
INSERT INTO sns_types (name) VALUES ('NAVER');
INSERT INTO sns_types (name) VALUES ('GOOGLE');

-- 파일
CREATE TABLE IF NOT EXISTS files
(
    id   INTEGER NOT NULL AUTO_INCREMENT,                 -- 파일아이디
    type ENUM('PHOTO', 'VIDEO') NOT NULL DEFAULT 'PHOTO', -- 파일 타입
    path VARCHAR(255) NOT NULL,                           -- 파일경로
    PRIMARY KEY (id)
);
INSERT INTO files (path) VALUES ('1');

-- 반려동물 타입
CREATE TABLE IF NOT EXISTS pet_types
(
    id            INTEGER     NOT NULL AUTO_INCREMENT, -- 반려동물 타입 아이디
    name VARCHAR(50) NOT NULL, -- 타입 이름
    PRIMARY KEY (id)
);
INSERT INTO pet_types (name) VALUES ('DOG');
INSERT INTO pet_types (name) VALUES ('CAT');
INSERT INTO pet_types (name) VALUES ('HAMSTER');
INSERT INTO pet_types (name) VALUES ('PARROT');
INSERT INTO pet_types (name) VALUES ('FISH');
INSERT INTO pet_types (name) VALUES ('ETC');

-- 유저
CREATE TABLE IF NOT EXISTS users
(
    id             INTEGER      NOT NULL AUTO_INCREMENT, -- 유저아이디
    nickname       VARCHAR(50)  NOT NULL,                -- 닉네임
    sns_account_id VARCHAR(255) NOT NULL,                -- sns계정고유값
    sns_type_id    INTEGER      NOT NULL,                -- sns타입아이디
    file_id        INTEGER      NOT NULL DEFAULT 1,      -- 파일아이디
    address        VARCHAR(255),                         -- 주소
    dong_name      VARCHAR(255),                         -- 동이름
    latitude DOUBLE,                                     -- 위도
    longitude DOUBLE,                                    -- 경도
    distance ENUM('1', '2', '3', '4') NULL,              -- 거리
    PRIMARY KEY (id),
    FOREIGN KEY (sns_type_id) REFERENCES sns_types (id),
    FOREIGN KEY (file_id) REFERENCES files (id),
    UNIQUE (nickname)
);

-- 반려동물
CREATE TABLE IF NOT EXISTS pets
(
    id              INTEGER      NOT NULL AUTO_INCREMENT,                 -- 반려동물 아이디
    owner_id        INTEGER      NOT NULL,                                -- 유저아이디
    pet_type_id     INTEGER      NOT NULL,                                -- 반려동물 타입 아이디
    name            VARCHAR(50)  NOT NULL,                                -- 이름
    gender          VARCHAR(50)  NOT NULL,                                -- 성별
    birth           VARCHAR(50)  NOT NULL,                                -- 생일
    weight DOUBLE NOT NULL,                                               -- 몸무게
    info            VARCHAR(255) NOT NULL,                                -- 간단한 설명
    neutered        BOOLEAN      NOT NULL,                                -- 중성화
    activity_status ENUM('WALK', 'PLAY', 'NONE') NOT NULL DEFAULT 'NONE', -- 활동상태
    PRIMARY KEY (id),
    FOREIGN KEY (owner_id) REFERENCES users (id),
    FOREIGN KEY (pet_type_id) REFERENCES pet_types (id)
);

-- 반려동물 사진
CREATE TABLE IF NOT EXISTS pet_photos
(
    file_id INTEGER NOT NULL, -- 파일아이디
    pet_id  INTEGER NOT NULL, -- 반려동물 아이디
    thumbnail BOOLEAN NOT NULL DEFAULT false, -- 대표 사진
    PRIMARY KEY (file_id, pet_id),
    FOREIGN KEY (file_id) REFERENCES files (id),
    FOREIGN KEY (pet_id) REFERENCES pets (id)
);

-- 펫시터
CREATE TABLE IF NOT EXISTS pet_sitters
(
    id          INTEGER     NOT NULL,               -- 유저아이디
    pet_type_id INTEGER NULL,                       -- 반려동물 타입 아이디
    age         VARCHAR(50) NOT NULL,               -- 연령대
    house_type  VARCHAR(50) NOT NULL,               -- 주거형태
    comment     VARCHAR(255) NULL,                  -- 설명
    grown       BOOLEAN     NOT NULL DEFAULT false, -- 현재 반려동물 여부
    pet_count   ENUM('ONE', 'TWO', 'THREE_PLUS') NULL, -- 반려동물 마릿수
    sitter_exp  BOOLEAN     NOT NULL DEFAULT false, -- 펫시터 경험
    file_id     INTEGER     NOT NULL,               -- 파일아이디
    created_at  DATETIME    NOT NULL DEFAULT current_timestamp(), -- 펫시터 등록날짜
    apply_at    DATETIME,                           -- 펫시터 승인날짜
    PRIMARY KEY (id),
    FOREIGN KEY (pet_type_id) REFERENCES pet_types (id),
    FOREIGN KEY (file_id) REFERENCES files (id),
    FOREIGN KEY (id) REFERENCES users (id)
);

-- 게시판 타입
CREATE TABLE IF NOT EXISTS board_types
(
    id   INTEGER     NOT NULL AUTO_INCREMENT, -- 게시판 타입 아이디
    name VARCHAR(50) NOT NULL,                -- 게시판 이름
    PRIMARY KEY (id)
);
INSERT INTO board_types (name) VALUES ('자유게시판');
INSERT INTO board_types (name) VALUES ('중고장터');
INSERT INTO board_types (name) VALUES ('정보게시판');

-- 게시판
CREATE TABLE IF NOT EXISTS boards
(
    id            INTEGER      NOT NULL AUTO_INCREMENT,              -- 게시판 아이디
    board_type_id INTEGER      NOT NULL,                             -- 게시판 타입 아이디
    title         VARCHAR(255) NOT NULL,                             -- 제목
    content       TEXT         NOT NULL,                             -- 내용
    user_id       INTEGER      NOT NULL,                             -- 유저아이디
    created_at    DATETIME     NOT NULL DEFAULT current_timestamp(), -- 작성날짜
    like_count    INTEGER      NOT NULL DEFAULT 0,                   -- 좋아요 갯수
    comment_count INTEGER      NOT NULL DEFAULT 0,                   -- 댓글 갯수
    PRIMARY KEY (id),
    FOREIGN KEY (board_type_id) REFERENCES board_types (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 게시판 사진
CREATE TABLE IF NOT EXISTS board_photos
(
    board_id INTEGER NOT NULL, -- 게시판 아이디
    file_id  INTEGER NOT NULL, -- 파일아이디
    PRIMARY KEY (board_id, file_id),
    FOREIGN KEY (board_id) REFERENCES boards (id),
    FOREIGN KEY (file_id) REFERENCES files (id)
);

-- 중고장터 게시판
CREATE TABLE IF NOT EXISTS products
(
    board_id INTEGER NOT NULL,               -- 게시판 아이디
    price    INTEGER NOT NULL,               -- 가격
    sell     BOOLEAN NOT NULL DEFAULT true,  -- 판매여부
    address  VARCHAR(255) NULL,              -- 주소
    PRIMARY KEY (board_id),
    FOREIGN KEY (board_id) REFERENCES boards (id)
);

-- 댓글
CREATE TABLE IF NOT EXISTS comments
(
    id         INTEGER  NOT NULL AUTO_INCREMENT,              -- 댓글 아이디
    user_id    INTEGER  NOT NULL,                             -- 유저아이디
    board_id   INTEGER  NOT NULL,                             -- 게시판 아이디
    content    TEXT     NOT NULL,                             -- 내용
    created_at DATETIME NOT NULL DEFAULT current_timestamp(), -- 작성날짜
    modified   BOOLEAN  NOT NULL DEFAULT false,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (board_id) REFERENCES boards (id)
);

-- 알람 타입
CREATE TABLE IF NOT EXISTS notification_types
(
    id   INTEGER     NOT NULL AUTO_INCREMENT, -- 알람 타입 아이디
    name VARCHAR(50) NOT NULL,                -- 알람 타입 이름
    PRIMARY KEY (id)
);
INSERT INTO notification_types (name) VALUES ('BOARD_COMMENT');
INSERT INTO notification_types (name) VALUES ('PETSTA_COMMENT');
INSERT INTO notification_types (name) VALUES ('FACILITY_DATE');
INSERT INTO notification_types (name) VALUES ('CHAT');
INSERT INTO notification_types (name) VALUES ('GLOBAL');

-- 알람
CREATE TABLE IF NOT EXISTS notifications
(
    id             INTEGER  NOT NULL AUTO_INCREMENT,              -- 알람 아이디
    user_id        INTEGER  NOT NULL,                             -- 유저아이디
    notify_type_id INTEGER  NOT NULL,                             -- 알람 타입 아이디
    content        TEXT     NOT NULL,                             -- 내용
    read_status    BOOLEAN  NOT NULL DEFAULT false,               -- 확인 여부
    created_at     DATETIME NOT NULL DEFAULT current_timestamp(), -- 알람 날짜
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (notify_type_id) REFERENCES notification_types (id)
);

-- 펫스타 게시판
CREATE TABLE IF NOT EXISTS petsta_posts
(
    id            INTEGER  NOT NULL AUTO_INCREMENT,              -- 펫스타 게시판 아이디
    user_id       INTEGER  NOT NULL,                             -- 유저아이디
    file_id       INTEGER  NOT NULL,                             -- 파일아이디
    content       TEXT     NOT NULL,                             -- 내용
    created_at    DATETIME NOT NULL DEFAULT current_timestamp(), -- 작성 날짜
    like_count    INTEGER  NOT NULL DEFAULT 0,                   -- 좋아요 갯수
    comment_count INTEGER  NOT NULL DEFAULT 0,                   -- 댓글 갯수
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (file_id) REFERENCES files (id)
);

-- 펫스타 댓글
CREATE TABLE IF NOT EXISTS petsta_comments
(
    id          INTEGER      NOT NULL AUTO_INCREMENT,              -- 펫스타 댓글 아이디
    post_id     INTEGER      NOT NULL,                             -- 펫스타 게시판 아이디
    user_id     INTEGER      NOT NULL,                             -- 유저아이디
    content     VARCHAR(255) NOT NULL,                             -- 내용
    parent_id   INTEGER NULL,                                      -- 부모댓글 아이디
    created_at  DATETIME     NOT NULL DEFAULT current_timestamp(), -- 작성 날짜
    reply_count INTEGER      NOT NULL DEFAULT 0,                   -- 대댓글 갯수
    PRIMARY KEY (id),
    FOREIGN KEY (post_id) REFERENCES petsta_posts (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (parent_id) REFERENCES petsta_comments (id)
);

-- 팔로우 테이블
CREATE TABLE IF NOT EXISTS user_follows
(
    follower_id INTEGER NOT NULL, -- 팔로워 아이디
    followed_id INTEGER NOT NULL, -- 팔로우 아이디
    PRIMARY KEY (follower_id, followed_id),
    FOREIGN KEY (follower_id) REFERENCES users (id),
    FOREIGN KEY (followed_id) REFERENCES users (id)
);

-- 채팅방 테이블
CREATE TABLE IF NOT EXISTS chat_rooms
(
    id          INTEGER  NOT NULL AUTO_INCREMENT, -- 채팅방 아이디
    user_id1    INTEGER  NOT NULL,                -- 유저아이디
    user_id2    INTEGER  NOT NULL,                -- 유저아이디2
    last_update DATETIME NOT NULL,                -- 마지막 채팅 시각
    PRIMARY KEY (id),
    FOREIGN KEY (user_id1) REFERENCES users (id),
    FOREIGN KEY (user_id2) REFERENCES users (id)
);

-- 채팅방 타입
CREATE TABLE IF NOT EXISTS chat_types
(
    id      INTEGER      NOT NULL AUTO_INCREMENT,   -- 채팅 타입
    name    VARCHAR(50)  NOT NULL,  -- 채팅 타입 이름
    PRIMARY KEY (id)
);
INSERT INTO chat_types (name) VALUES ('TEXT');
INSERT INTO chat_types (name) VALUES ('MATCH');
INSERT INTO chat_types (name) VALUES ('TRADE');

-- 채팅방 메시지 테이블
CREATE TABLE IF NOT EXISTS message
(
    id           INTEGER      NOT NULL AUTO_INCREMENT,              -- 메시지 아이디
    chat_room_id INTEGER      NOT NULL,                             -- 채팅방 아이디
    user_id      INTEGER      NOT NULL,                             -- 유저아이디
    content      VARCHAR(255) NOT NULL,                             -- 내용
    created_at   DATETIME     NOT NULL DEFAULT current_timestamp(), -- 작성 날짜
    type_id      INTEGER      NOT NULL, -- 채팅 타입
    metadata     JSON,  -- 메타데이터
    PRIMARY KEY (id),
    FOREIGN KEY (chat_room_id) REFERENCES chat_rooms (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (type_id) REFERENCES chat_types (id)
);


-- 이벤트
CREATE TABLE IF NOT EXISTS events
(
    id         INTEGER      NOT NULL AUTO_INCREMENT, -- 이벤트 아이디
    title      VARCHAR(255) NOT NULL,                -- 이벤트이름
    address    VARCHAR(255) NULL,                    -- 주소
    start_date DATETIME     NOT NULL,                -- 시작 날짜
    end_date   DATETIME     NOT NULL,                -- 종료 날짜
    event_url  VARCHAR(255) NULL,                    -- 이벤트 웹주소
    latitude DOUBLE NULL,                            -- 위도
    longitude DOUBLE NULL,                           -- 경도
    PRIMARY KEY (id)
);

-- 캘린더 일정
CREATE TABLE IF NOT EXISTS schedules
(
    id             INTEGER      NOT NULL AUTO_INCREMENT, -- 캘린더 일정 아이디
    user_id        INTEGER      NOT NULL,                -- 유저아이디
    title          VARCHAR(255) NOT NULL,                -- 일정 제목
    content        TEXT         NOT NULL,                -- 일정 내용
    start_date     DATETIME     NOT NULL,                -- 시작 날짜
    end_date       DATETIME NULL,                        -- 종료 날짜
    address        VARCHAR(255) NOT NULL,                -- 주소
    latitude DOUBLE NOT NULL,                            -- 위도
    longitude DOUBLE NOT NULL,                           -- 경도
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 편의시설 타입
CREATE TABLE IF NOT EXISTS facility_types
(
    id   INTEGER     NOT NULL AUTO_INCREMENT, -- 편의시설 타입 아이디
    name VARCHAR(50) NOT NULL,                -- 편의시설 타입
    PRIMARY KEY (id)
);
INSERT INTO facility_types (name) VALUES ('HOTEL');
INSERT INTO facility_types (name) VALUES ('BEAUTY');
INSERT INTO facility_types (name) VALUES ('CAFE');

-- 편의시설
CREATE TABLE IF NOT EXISTS facilities
(
    id               INTEGER      NOT NULL AUTO_INCREMENT, -- 편의시설 아이디
    facility_type_id INTEGER      NOT NULL,                -- 편의시설 타입 아이디
    name             VARCHAR(255) NOT NULL,                -- 편의시설 이름
    tel              VARCHAR(50) NULL,                     -- 전화번호
    comment          TEXT NULL,                            -- 설명
    star_point       DOUBLE NOT NULL DEFAULT 0.0,          -- 별점
    review_count     INTEGER NOT NULL DEFAULT 0,           -- 리뷰 갯수
    address          VARCHAR(255) NOT NULL,                -- 주소
    detail_address   VARCHAR(255) NULL,                    -- 상세주소
    latitude DOUBLE NOT NULL,                              -- 위도
    longitude DOUBLE NOT NULL,                             -- 경도
    PRIMARY KEY (id),
    FOREIGN KEY (facility_type_id) REFERENCES facility_types (id)
);

-- 편의시설 시간표
CREATE TABLE IF NOT EXISTS facility_timetables
(
    id               INTEGER      NOT NULL AUTO_INCREMENT, -- 편의시설 시간표 아이디
    facility_id		 INTEGER      NOT NULL,                -- 편의시설 아이디
    day 			 ENUM('MON','TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN') NOT NULL, -- 날짜
    open_time        TIME NULL,                            -- 개점시각
    close_time       TIME NULL,                            -- 폐점시각
    PRIMARY KEY (id),
    FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- 편의시설 사진
CREATE TABLE IF NOT EXISTS facility_photos
(
    file_id     INTEGER NOT NULL, -- 파일아이디
    facility_id INTEGER NOT NULL, -- 편의시설 아이디
    PRIMARY KEY (file_id, facility_id),
    FOREIGN KEY (facility_id) REFERENCES facilities (id),
    FOREIGN KEY (file_id) REFERENCES files (id)
);

-- 예약
CREATE TABLE IF NOT EXISTS reserves
(
    id             INTEGER  NOT NULL AUTO_INCREMENT, -- 예약 아이디
    user_id        INTEGER  NOT NULL,                -- 유저아이디
    facility_id    INTEGER  NOT NULL,                -- 편의시설 아이디
    entry_time     DATETIME NOT NULL,                -- 예약 시작시각
    exit_time      DATETIME NULL,                    -- 예약 종료시각
    amount         INTEGER  NOT NULL,                -- 예약금
    reserve_status BOOLEAN  NOT NULL DEFAULT false,  -- 예약 완료 상태
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY(facility_id) REFERENCES facilities (id)
);

-- 결제내역
CREATE TABLE IF NOT EXISTS payments
(
    id         INTEGER      NOT NULL AUTO_INCREMENT,              -- 결제내역 아이디
    uuid       VARCHAR(255) NOT NULL,                             -- 결제고유번호
    reserve_id INTEGER      NOT NULL,                             -- 예약 아이디
    price      INTEGER      NOT NULL,                             -- 가격
    created_at DATETIME     NOT NULL DEFAULT current_timestamp(), -- 결제 일자
    PRIMARY KEY (id),
    FOREIGN KEY (reserve_id) REFERENCES reserves (id),
    UNIQUE (reserve_id)
);

-- 편의시설 리뷰
CREATE TABLE IF NOT EXISTS reviews
(
    id          INTEGER NOT NULL AUTO_INCREMENT, -- 편의시설 리뷰 아이디
    user_id     INTEGER NOT NULL,                -- 유저아이디
    facility_id INTEGER NOT NULL,                -- 편의시설 아이디
    comment     TEXT NULL,                       -- 리뷰내용
    star_point  INTEGER NOT NULL,                -- 별점
    created_at  DATETIME NOT NULL DEFAULT current_timestamp(), -- 작성일자
    PRIMARY KEY (id),
    FOREIGN KEY (facility_id) REFERENCES facilities (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 관리자
CREATE TABLE IF NOT EXISTS admins
(
    id       INTEGER      NOT NULL AUTO_INCREMENT, -- 관리자 아이디
    email    VARCHAR(50)  NOT NULL,                -- 이메일
    password VARCHAR(255) NOT NULL,                -- 비밀번호
    PRIMARY KEY (id),
    UNIQUE (email)
);

-- 공지
CREATE TABLE IF NOT EXISTS announces
(
    id            INTEGER      NOT NULL AUTO_INCREMENT,              -- 공지 아이디
    title         VARCHAR(255) NOT NULL,                             -- 제목
    content       TEXT         NOT NULL,                             -- 내용
    created_at    DATETIME     NOT NULL DEFAULT current_timestamp(), -- 생성일
    board_type_id INTEGER      NOT NULL,                             -- 게시판 타입 아이디
    PRIMARY KEY (id),
    FOREIGN KEY (board_type_id) REFERENCES board_types (id)
);

-- 공지 사진
CREATE TABLE IF NOT EXISTS announce_photos
(
    announce_id INTEGER NOT NULL, -- 공지 아이디
    file_id     INTEGER NOT NULL, -- 파일아이디
    PRIMARY KEY (announce_id, file_id),
    FOREIGN KEY (announce_id) REFERENCES announces (id),
    FOREIGN KEY (file_id) REFERENCES files (id)
);

-- 게시판 북마크
CREATE TABLE IF NOT EXISTS board_bookmarks
(
    user_id  INTEGER NOT NULL, -- 유저아이디
    board_post_id INTEGER NOT NULL, -- 게시판 아이디
    PRIMARY KEY (user_id, board_post_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (board_post_id) REFERENCES boards (id)
);


-- 펫스타 북마크
CREATE TABLE IF NOT EXISTS petsta_bookmarks
(
    user_id        INTEGER NOT NULL, -- 유저아이디
    petsta_post_id INTEGER NOT NULL, -- 펫스타 게시판 아이디
    PRIMARY KEY (user_id, petsta_post_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (petsta_post_id) REFERENCES petsta_posts (id)
);

-- 게시판 좋아요
CREATE TABLE IF NOT EXISTS board_likes
(
    user_id  INTEGER NOT NULL, -- 유저아이디
    board_post_id INTEGER NOT NULL, -- 게시판 아이디
    PRIMARY KEY (user_id, board_post_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (board_post_id) REFERENCES boards (id)
);

-- 펫스타 좋아요
CREATE TABLE IF NOT EXISTS petsta_likes
(
    user_id   INTEGER NOT NULL, -- 유저아이디
    petsta_post_id INTEGER NOT NULL, -- 펫스타 게시판 아이디
    PRIMARY KEY (user_id, petsta_post_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (petsta_post_id) REFERENCES petsta_posts (id)
);