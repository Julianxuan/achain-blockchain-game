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

CREATE TABLE `blockchain_dog_info` (
  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `dog_id` INT(11) UNSIGNED NOT NULL COMMENT '加密狗的编号',
  `nickname` VARCHAR(256) DEFAULT NULL COMMENT '加密狗的昵称',
  `owner` VARCHAR(70) NOT NULL COMMENT '所属主人地址',
  `gene` VARCHAR(256) NOT NULL COMMENT '狗的基因序列',
  `birth_time` TIMESTAMP NOT NULL COMMENT '出生日期',
  `cooldown_end_time` DATETIME NOT NULL COMMENT '繁衍的冷却结束时间',
  `mother_id` INT(11) UNSIGNED NOT NULL COMMENT '加密狗的母亲编号',
  `father_id` INT(11) UNSIGNED NOT NULL COMMENT '加密狗的父亲的编号',
  `is_pregnant` TINYINT NOT NULL COMMENT '是否怀孕,0-否,1-是',
  `generation` INT(11) NOT NULL COMMENT '第几代',
  `fertility` TINYINT NOT NULL COMMENT '是否可育,0-否,1-是',
  `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner`(`owner`),
  UNIQUE KEY `idx_dog_id`(`dog_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='加密狗信息表';


CREATE TABLE `blockchain_dog_order` (
  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `dog_id` INT(11) UNSIGNED NOT NULL COMMENT '买卖加密狗的编号',
  `order_id` VARCHAR(256) NOT NULL COMMENT '挂单订单号',
  `seller` VARCHAR(70) NOT NULL COMMENT '挂单人的地址',
  `buyer` VARCHAR(70) DEFAULT NULL COMMENT '买方的地址',
  `starting_price` BIGINT UNSIGNED COMMENT '买卖狗的起始价格',
  `ending_price` BIGINT UNSIGNED COMMENT '买卖狗的结束价格',
  `trans_price` BIGINT DEFAULT 0 COMMENT '成交价格',
  `status` TINYINT NOT NULL COMMENT '订单状态,0-进行中,1-交易成功,2-交易取消,3-交易失效,4-交易失败',
  `trx_id` VARCHAR(70) DEFAULT NULL COMMENT '链上的交易单号',
  `begin_time` TIMESTAMP NOT NULL COMMENT '订单开始时间',
  `end_time` DATETIME NOT NULL COMMENT '订单结束时间',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_trx_id`(`trx_id`),
  KEY `idx_dog_id`(`dog_id`),
  UNIQUE KEY (`order_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='加密狗交易订单表';


CREATE TABLE `blockchain_dog_meting_order` (
  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
 `order_id` VARCHAR(256) NOT NULL COMMENT '挂单订单号',
  `seller_dog_id` INT(11) UNSIGNED NOT NULL COMMENT '配种狗的编号',
  `buyer_dog_id` INT(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '进行配种狗的编号',
  `seller` VARCHAR(70) NOT NULL COMMENT '挂单人的地址',
  `buyer` VARCHAR(70) DEFAULT NULL COMMENT '买方的地址',
  `starting_price` BIGINT UNSIGNED COMMENT '起始价格',
  `ending_price` BIGINT UNSIGNED COMMENT '结束价格',
  `trans_price` BIGINT DEFAULT 0 COMMENT '成交价格',
  `status` TINYINT NOT NULL COMMENT '订单状态,0-进行中,1-交易成功,2-交易取消,3-交易失效,4-交易失败',
  `trx_id` VARCHAR(70) DEFAULT NULL COMMENT '链上的交易单号',
  `begin_time` TIMESTAMP NOT NULL COMMENT '订单开始时间',
  `end_time` DATETIME NOT NULL COMMENT '订单结束时间',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_trx_id`(`trx_id`),
  KEY `idx_seller_dog_id`(`seller_dog_id`),
  KEY `idx_buyer_dog_id`(`buyer_dog_id`),
  UNIQUE KEY (`order_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='加密狗繁衍订单表';

CREATE TABLE `blockchain_dog_user_order` (
  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `recharge_trx_id` VARCHAR(70) DEFAULT NULL COMMENT '充值的trxId',
  `recharge_status` TINYINT DEFAULT 0 COMMENT '充值的状态,0-进行中,1-成功,4-失败',
  `trx_id` VARCHAR(70) DEFAULT NULL COMMENT '链上的交易单号',
  `method` VARCHAR(64) DEFAULT NULL COMMENT '调用的合约方法名',
  `status` TINYINT DEFAULT 0 COMMENT '订单状态,0-进行中,1-交易成功,4-交易失败',
  `message` VARCHAR(255) DEFAULT NULL COMMENT '信息描述',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`trx_id`),
  UNIQUE KEY (`recharge_trx_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='加密狗合约调用结果表';





