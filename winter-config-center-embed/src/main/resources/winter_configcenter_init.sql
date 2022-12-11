create table if not exists `winter_config_center`
(
    `id`            bigint(20) unsigned not null auto_increment comment '自增主键',
    `name`          varchar(64)         not null comment '配置名称',
    `group`         varchar(64)         not null comment '配置分组',
    `content`       mediumtext          not null comment '配置内容',
    `last_modified` datetime(3)         not null default current_timestamp(3) comment '配置最后修改时间，精度到毫秒',
    `description`   varchar(255)                 default null comment '配置描述',
    primary key (`id`),
    unique key `uk_name_group` (`name`, `group`) using btree
) engine = InnoDB
  auto_increment = 1
  default charset = UTF8MB4;