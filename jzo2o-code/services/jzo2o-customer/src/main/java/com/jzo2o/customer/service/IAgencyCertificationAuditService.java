package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;

/**
 * 机构认证审核服务
 */
public interface IAgencyCertificationAuditService extends IService<AgencyCertificationAudit> {

    PageResult<AgencyCertificationAuditResDTO> pageQuery(
            AgencyCertificationAuditPageQueryReqDTO queryReqDTO);

    void auditCertification(Long id, CertificationAuditReqDTO certificationAuditReqDTO);

    void applyCertification(AgencyCertificationAuditAddReqDTO addReqDTO);

    RejectReasonResDTO queryCurrentUserLastRejectReason();
}
