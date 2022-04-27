CREATE TABLE `sys_menu`
(
    `kid`            bigint       NOT NULL,
    `pid`            bigint       NOT NULL,
    `grandfather_id` bigint       NOT NULL,
    `name`           varchar(128) NOT NULL,
    `value_left`     int          NOT NULL,
    `value_right`    int          NOT NULL,
    `level`          int          NOT NULL,
    PRIMARY KEY (`kid`) USING BTREE
) ENGINE = InnoDB;