-- 银行账户信息表
CREATE TABLE IF NOT EXISTS `bank_account` (
  `id` bigint NOT NULL COMMENT '服务人员/机构id',
  `type` int DEFAULT NULL COMMENT '类型，2：服务人员，3：服务机构',
  `name` varchar(50) DEFAULT NULL COMMENT '户名',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '银行名称',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区',
  `branch` varchar(100) DEFAULT NULL COMMENT '网点',
  `account` varchar(50) DEFAULT NULL COMMENT '银行账号',
  `account_certification` varchar(255) DEFAULT NULL COMMENT '开户证明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='银行账户信息表';
