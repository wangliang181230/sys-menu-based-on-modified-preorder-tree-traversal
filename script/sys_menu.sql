CREATE TABLE `sys_menu`
(
    `id`      bigint       NOT NULL,
    `pid`     bigint       NOT NULL,
    `root_id` bigint       NOT NULL,
    `name`    varchar(128) NOT NULL,
    `l`       int          NOT NULL,
    `r`       int          NOT NULL,
    `level`   int          NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB;