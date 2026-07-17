package com.jzo2o.customer.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 运营端机构认证审核接口
 */
@RestController("operationAgencyCertificationAuditController")
@RequestMapping("/operation/agency-certification-audit")
@Api(tags = "运营端 - 机构认证审核相关接口")
public class AgencyCertificationAuditController {

    @Resource
    private IAgencyCertificationAuditService agencyCertificationAuditService;

    @GetMapping("/page")
    @ApiOperation("机构认证审核信息分页查询")
    public PageResult<AgencyCertificationAuditResDTO> page(
            AgencyCertificationAuditPageQueryReqDTO queryReqDTO) {
        return agencyCertificationAuditService.pageQuery(queryReqDTO);
    }

    @PutMapping("/audit/{id}")
    @ApiOperation("审核机构认证信息")
    public void auditCertification(@PathVariable("id") Long id,
                                   CertificationAuditReqDTO certificationAuditReqDTO) {
        agencyCertificationAuditService.auditCertification(id, certificationAuditReqDTO);
    }
}
