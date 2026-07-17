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
 * 机构认证审核记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("agency_certification_audit")
public class AgencyCertificationAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long serveProviderId;

    private String name;

    private String idNumber;

    private String legalPersonName;

    private String legalPersonIdCardNo;

    private String businessLicense;

    /**
     * 审核状态，0：未审核，1：已审核
     */
    private Integer auditStatus;

    private Long auditorId;

    private String auditorName;

    private LocalDateTime auditTime;

    /**
     * 认证状态，1：认证中，2：认证成功，3：认证失败
     */
    private Integer certificationStatus;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
