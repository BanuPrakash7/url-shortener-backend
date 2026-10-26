CREATE
DATABASE IF NOT EXISTS urlshortener;
USE
urlshortener;
CREATE TABLE `users`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `email`         varchar(255) NOT NULL,
    `password_hash` varchar(255) NOT NULL,
    `created_at`    datetime(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`)
);

CREATE TABLE `urls`
(
    `id`         bigint                                            NOT NULL AUTO_INCREMENT,
    `short_code` varchar(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    `long_url`   varchar(2048)                                     NOT NULL,
    `user_id`    bigint                                            NOT NULL,
    `expires_at` datetime(6) DEFAULT NULL,
    `created_at` datetime(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_urls_short_code` (`short_code`)
);

CREATE TABLE clicks
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
    url_id     BIGINT NOT NULL,
    clicked_at DATETIME(6)   NOT NULL,
    referrer   VARCHAR(2048) NULL,
    user_agent VARCHAR(512) NULL,
    PRIMARY KEY (id),
    KEY        idx_clicks_url_id (url_id)
);

CREATE TABLE `refresh_tokens`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT,
    `user_id`    bigint       NOT NULL,
    `token`      varchar(255) NOT NULL,
    `expires_at` datetime(6) NOT NULL,
    `created_at` datetime(6) NOT NULL,
    `revoked`    tinyint(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_tokens_token` (`token`),
    KEY          `idx_refresh_tokens_user_id` (`user_id`)
);