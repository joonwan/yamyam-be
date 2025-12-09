-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS yumyum_coach CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yumyum_coach;

-- 사용자 테이블
CREATE TABLE users
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    email      VARCHAR(100) UNIQUE NOT NULL,
    password   VARCHAR(100)        NOT NULL,
    nickname   VARCHAR(100) UNIQUE NOT NULL,
    name       VARCHAR(100)        NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 문자열 비교 및 정렬 규칙 옵션. utf8mb4: 문자 인코딩 방식(이모지 포함 모든 유니코드 지원), unicode: 유니코드 표준에 따른 정렬, ci: Case Insensitive: 대소문자 구분 안함

CREATE TABLE body_specs
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT      NOT NULL,
    height  INT         NOT NULL COMMENT '키(cm)',
    weight  INT         NOT NULL COMMENT '체중(kg)',
    age     INT         NOT NULL COMMENT '나이',
    gender  VARCHAR(50) NOT NULL COMMENT '성별',
    date    DATETIME    NOT NULL COMMENT '측정일',
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE follows
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL COMMENT '팔로우하는 사용자',
    followed_id BIGINT NOT NULL COMMENT '팔로우받는 사용자',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (followed_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY unique_follow (follower_id, followed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE foods
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(200) NOT NULL COMMENT '음식명',
    category  VARCHAR(100) COMMENT '카테고리',

    -- 기준 단위
    base_unit VARCHAR(10)  NOT NULL DEFAULT 'g' COMMENT '기준 단위 (g 또는 ml)',

    -- 100 단위 기준 영양소
    energy_per_100 DOUBLE COMMENT '에너지(kcal/100단위)',
    protein_per_100 DOUBLE COMMENT '단백질(g/100단위)',
    fat_per_100 DOUBLE COMMENT '지방(g/100단위)',
    carbohydrate_per_100 DOUBLE COMMENT '탄수화물(g/100단위)',
    sugar_per_100 DOUBLE COMMENT '당(g/100단위)',
    sodium_per_100 DOUBLE COMMENT '나트륨(mg/100단위)',
    cholesterol_per_100 DOUBLE COMMENT '콜레스테롤(mg/100단위)',
    saturated_fat_per_100 DOUBLE COMMENT '포화지방(g/100단위)',
    trans_fat_per_100 DOUBLE COMMENT '트랜스지방(g/100단위)'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE diet_plans
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT  NOT NULL,
    created_at DATETIME         DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    content    LONGTEXT COMMENT '식단 계획 내용',
    start_date DATE COMMENT '시작일',
    end_date   DATE COMMENT '종료일',
    title      VARCHAR(255) COMMENT '제목',
    is_shared  BOOLEAN          DEFAULT FALSE COMMENT '공유여부',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE COMMENT '대표 식단 여부',
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE INDEX ux_user_primary_diet (
        user_id,
        (CASE WHEN is_primary = TRUE THEN 1 ELSE NULL END)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE daily_diets
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    diet_plan_id BIGINT NOT NULL,
    date         DATE   NOT NULL COMMENT '식단 날짜',
    description  TEXT COMMENT '메모',
    FOREIGN KEY (diet_plan_id) REFERENCES diet_plans (id) ON DELETE CASCADE,
    UNIQUE KEY unique_diet_plan_date (diet_plan_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE meals
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    daily_diet_id BIGINT       NOT NULL,
    type          VARCHAR(100) NOT NULL COMMENT '식사 타입(아침/점심/저녁/간식)',
    FOREIGN KEY (daily_diet_id) REFERENCES daily_diets (id) ON DELETE CASCADE,
    UNIQUE KEY unique_daily_diet_meal_type (daily_diet_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE meal_foods
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity DOUBLE NOT NULL COMMENT '섭취량',

    FOREIGN KEY (meal_id) REFERENCES meals (id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE challenges
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(1000) NOT NULL COMMENT '챌린지 제목',
    description TEXT COMMENT '챌린지 설명',
    start_date  DATETIME      NOT NULL COMMENT '시작일',
    end_date    DATETIME      NOT NULL COMMENT '종료일',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE challenge_participations
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    challenge_id BIGINT       NOT NULL,
    status       VARCHAR(100) NOT NULL COMMENT '참여 상태(진행중/완료/중단)',
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE,
    UNIQUE KEY unique_participation (user_id, challenge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글 테이블
CREATE TABLE posts
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    diet_plan_id BIGINT COMMENT '연관된 식단 계획 ID',
    user_id      BIGINT       NOT NULL,
    title        VARCHAR(100) NOT NULL COMMENT '제목',
    content      TEXT         NOT NULL COMMENT '내용',
    copy_count   INT      DEFAULT 0 COMMENT '스크랩 수',
    like_count   INT      DEFAULT 0 COMMENT '좋아요 수',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (diet_plan_id) REFERENCES diet_plans (id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE post_likes
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY unique_like (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comments
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    content    TEXT   NOT NULL COMMENT '댓글 내용',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;