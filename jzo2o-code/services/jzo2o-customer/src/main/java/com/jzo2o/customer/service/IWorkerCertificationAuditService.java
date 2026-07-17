package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;

/**
 * 服务人员认证审核服务
 */
public interface IWorkerCertificationAuditService extends IService<WorkerCertificationAudit> {

    void applyCertification(WorkerCertificationAuditAddReqDTO addReqDTO);

    RejectReasonResDTO queryCurrentUserLastRejectReason();

    PageResult<WorkerCertificationAuditResDTO> pageQuery(
            WorkerCertificationAuditPageQueryReqDTO queryReqDTO);

    void auditCertification(Long id, CertificationAuditReqDTO certificationAuditReqDTO);
}
