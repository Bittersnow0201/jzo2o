-- 机构认证审核记录表
CREATE TABLE IF NOT EXISTS `agency_certification_audit` (
  `id` bigint NOT NULL COMMENT '主键',
  `serve_provider_id` bigint DEFAULT NULL COMMENT '机构id',
  `name` varchar(50) DEFAULT NULL COMMENT '企业名称',
  `id_number` varchar(50) DEFAULT NULL COMMENT '统一社会信用代码',
  `legal_person_name` varchar(50) DEFAULT NULL COMMENT '法人姓名',
  `legal_person_id_card_no` varchar(50) DEFAULT NULL COMMENT '法人身份证号',
  `business_license` varchar(100) DEFAULT NULL COMMENT '营业执照',
  `audit_status` int NOT NULL DEFAULT '0' COMMENT '审核状态，0：未审核，1：已审核',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人id',
  `auditor_name` varchar(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `certification_status` int NOT NULL DEFAULT '1' COMMENT '认证状态，1：认证中，2：认证成功，3：认证失败',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_serve_provider_id` (`serve_provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机构认证审核记录表';

-- 服务人员认证审核记录表
CREATE TABLE IF NOT EXISTS `worker_certification_audit` (
  `id` bigint NOT NULL COMMENT '主键',
  `serve_provider_id` bigint DEFAULT NULL COMMENT '服务人员id',
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `id_card_no` varchar(50) DEFAULT NULL COMMENT '身份证号',
  `front_img` varchar(100) DEFAULT NULL COMMENT '身份证正面',
  `back_img` varchar(100) DEFAULT NULL COMMENT '身份证反面',
  `certification_material` varchar(100) DEFAULT NULL COMMENT '证明材料',
  `audit_status` int NOT NULL DEFAULT '0' COMMENT '审核状态，0：未审核，1：已审核',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人id',
  `auditor_name` varchar(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `certification_status` int NOT NULL DEFAULT '1' COMMENT '认证状态，1：认证中，2：认证成功，3：认证失败',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_serve_provider_id` (`serve_provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务人员认证审核记录表';
