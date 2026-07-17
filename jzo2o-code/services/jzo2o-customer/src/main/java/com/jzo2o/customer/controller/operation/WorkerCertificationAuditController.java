package com.jzo2o.customer.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 运营端服务人员认证审核接口
 */
@RestController("operationWorkerCertificationAuditController")
@RequestMapping("/operation/worker-certification-audit")
@Api(tags = "运营端 - 服务人员认证审核相关接口")
public class WorkerCertificationAuditController {

    @Resource
    private IWorkerCertificationAuditService workerCertificationAuditService;

    @GetMapping("/page")
    @ApiOperation("服务人员认证审核信息分页查询")
    public PageResult<WorkerCertificationAuditResDTO> page(
            WorkerCertificationAuditPageQueryReqDTO queryReqDTO) {
        return workerCertificationAuditService.pageQuery(queryReqDTO);
    }

    @PutMapping("/audit/{id}")
    @ApiOperation("审核服务人员认证信息")
    public void auditCertification(@PathVariable("id") Long id,
                                   CertificationAuditReqDTO certificationAuditReqDTO) {
        workerCertificationAuditService.auditCertification(id, certificationAuditReqDTO);
    }
}
