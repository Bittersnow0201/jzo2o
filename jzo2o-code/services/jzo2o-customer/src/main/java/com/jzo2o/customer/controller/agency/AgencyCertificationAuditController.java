package com.jzo2o.customer.controller.agency;

import com.jzo2o.common.utils.UserContext;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 机构端认证申请接口
 */
@RestController("agencyAgencyCertificationAuditController")
@RequestMapping("/agency/agency-certification-audit")
@Api(tags = "机构端 - 机构认证审核相关接口")
public class AgencyCertificationAuditController {

    @Resource
    private IAgencyCertificationAuditService agencyCertificationAuditService;

    @PostMapping
    @ApiOperation("提交认证申请")
    public void applyCertification(@RequestBody AgencyCertificationAuditAddReqDTO addReqDTO) {
        addReqDTO.setServeProviderId(UserContext.currentUserId());
        agencyCertificationAuditService.applyCertification(addReqDTO);
    }

    @GetMapping("/rejectReason")
    @ApiOperation("查询最新的驳回原因")
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {
        return agencyCertificationAuditService.queryCurrentUserLastRejectReason();
    }
}
