package com.jzo2o.customer.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务人员认证审核记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("worker_certification_audit")
public class WorkerCertificationAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long serveProviderId;

    private String name;

    private String idCardNo;

    private String frontImg;

    private String backImg;

    private String certificationMaterial;

    private Integer auditStatus;

    private Long auditorId;

    private String auditorName;

    private LocalDateTime auditTime;

    private Integer certificationStatus;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
