CREATE TABLE `winter_jobdo`
(
    `id`                bigint AUTO_INCREMENT NOT NULL,
    `gmt_create`        datetime(6)  DEFAULT NULL,
    `gmt_modified`      datetime(6)  DEFAULT NULL,
    `name`              varchar(255) DEFAULT NULL,
    `class_name`        varchar(255) DEFAULT NULL,
    `begin_time`        datetime(6)  DEFAULT NULL,
    `end_time`          datetime(6)  DEFAULT NULL,
    `next_trigger_time` datetime(6)  DEFAULT NULL,
    `status`            varchar(255) DEFAULT NULL,
    `job_type`          varchar(255) DEFAULT NULL,
    `time_type`         varchar(255) DEFAULT NULL,
    `time_expression`   varchar(255) DEFAULT NULL,
    `extra_info`        varchar(255) DEFAULT NULL,
    `features`          varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classname` (`class_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE `winter_job_instancedo`
(
    `id`            bigint AUTO_INCREMENT NOT NULL,
    `gmt_create`    datetime(6)  DEFAULT NULL,
    `gmt_modified`  datetime(6)  DEFAULT NULL,
    `job_id`        bigint       DEFAULT NULL,
    `job_name`      varchar(255) DEFAULT NULL,
    `period_time`   datetime(6)  DEFAULT NULL,
    `begin_time`    datetime(6)  DEFAULT NULL,
    `end_time`      datetime(6)  DEFAULT NULL,
    `status`        varchar(255) DEFAULT NULL,
    `message`       varchar(255) DEFAULT NULL,
    `class_name`    varchar(255) DEFAULT NULL,
    `extra_info`    varchar(255) DEFAULT NULL,
    `manual_params` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;