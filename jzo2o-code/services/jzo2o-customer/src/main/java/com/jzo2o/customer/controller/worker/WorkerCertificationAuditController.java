package com.jzo2o.customer.controller.worker;

import com.jzo2o.common.utils.UserContext;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 服务人员端认证申请接口
 */
@RestController("workerWorkerCertificationAuditController")
@RequestMapping("/worker/worker-certification-audit")
@Api(tags = "服务人员端 - 实名认证审核相关接口")
public class WorkerCertificationAuditController {

    @Resource
    private IWorkerCertificationAuditService workerCertificationAuditService;

    @PostMapping
    @ApiOperation("提交实名认证申请")
    public void applyCertification(@RequestBody WorkerCertificationAuditAddReqDTO addReqDTO) {
        addReqDTO.setServeProviderId(UserContext.currentUserId());
        workerCertificationAuditService.applyCertification(addReqDTO);
    }

    @GetMapping("/rejectReason")
    @ApiOperation("查询最新的驳回原因")
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {
        return workerCertificationAuditService.queryCurrentUserLastRejectReason();
    }
}
