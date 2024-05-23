create table if not exists `winter_embed_config_center`
(
    `id`           bigint(20) unsigned not null auto_increment comment '自增主键',
    `gmt_create`   datetime(3)         not null default current_timestamp(3) comment '配置创建时间，精度到毫秒',
    `gmt_modified` datetime(3)         not null default current_timestamp(3) comment '配置最后修改时间，精度到毫秒',
    `name`         varchar(127)        not null comment '配置名称，最长127位',
    `group_name`   varchar(64)         not null comment '配置分组，最长64位',
    `content`      mediumtext          not null comment '配置内容',
    `description`  varchar(255)                 default null comment '配置描述',
    primary key (`id`),
    unique key `uk_name_group` (`name`, `group_name`) using btree
) engine = InnoDB
  auto_increment = 1
  default charset = UTF8MB4
    comment '配置表';

create table if not exists `winter_embed_config_center_history`
(
    `id`           bigint(20) unsigned not null auto_increment comment '自增主键',
    `gmt_create`   datetime(3)         not null default current_timestamp(3) comment '配置创建时间，精度到毫秒',
    `gmt_modified` datetime(3)         not null default current_timestamp(3) comment '配置最后修改时间，精度到毫秒',
    `name`         varchar(127)        not null comment '配置名称，最长127位',
    `group_name`   varchar(64)         not null comment '配置分组，最长64位',
    `content`      mediumtext          not null comment '配置内容',
    `description`  varchar(255)                 default null comment '配置描述',
    primary key (`id`),
    key `idx_name_group` (`name`, `group_name`) using btree
) engine = InnoDB
  auto_increment = 1
  default charset = UTF8MB4
    comment '配置表历史记录';