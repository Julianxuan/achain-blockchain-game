CREATE DATABASE blockchain_game;
USE blockchain_game;
CREATE TABLE `blockchain_record` (
  `id`          INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `block_num`   BIGINT           NOT NULL
  COMMENT '交易所在块号',
  `trx_id`      VARCHAR(64)               DEFAULT NULL
  COMMENT '交易id',
  `contract_id` VARCHAR(100)     NOT NULL
  COMMENT '合约id',
  `trx_time`    TIMESTAMP        NOT NULL
  COMMENT '交易时间',
  `create_time` TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)

)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='游戏交易记录表';


