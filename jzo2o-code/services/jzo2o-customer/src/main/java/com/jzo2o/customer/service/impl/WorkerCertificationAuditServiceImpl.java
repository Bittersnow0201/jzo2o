package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.enums.EnableStatusEnum;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.customer.enums.CertificationStatusEnum;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.WorkerCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.service.IServeProviderService;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.jzo2o.customer.service.IWorkerCertificationService;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 服务人员认证审核服务实现
 */
@Service
public class WorkerCertificationAuditServiceImpl
        extends ServiceImpl<WorkerCertificationAuditMapper, WorkerCertificationAudit>
        implements IWorkerCertificationAuditService {

    @Resource
    private IWorkerCertificationService workerCertificationService;

    @Resource
    private IServeProviderService serveProviderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCertification(Long id, CertificationAuditReqDTO certificationAuditReqDTO) {
        WorkerCertificationAudit audit = baseMapper.selectById(id);
        if (audit == null) {
            throw new BadRequestException("认证申请不存在");
        }

        CurrentUserInfo currentUserInfo = UserContext.currentUser();
        LambdaUpdateWrapper<WorkerCertificationAudit> updateWrapper =
                Wrappers.<WorkerCertificationAudit>lambdaUpdate()
                        .eq(WorkerCertificationAudit::getId, id)
                        .set(WorkerCertificationAudit::getAuditStatus,
                                EnableStatusEnum.ENABLE.getStatus())
                        .set(WorkerCertificationAudit::getAuditorId, currentUserInfo.getId())
                        .set(WorkerCertificationAudit::getAuditorName, currentUserInfo.getName())
                        .set(WorkerCertificationAudit::getAuditTime, LocalDateTime.now())
                        .set(WorkerCertificationAudit::getCertificationStatus,
                                certificationAuditReqDTO.getCertificationStatus())
                        .set(ObjectUtil.isNotEmpty(certificationAuditReqDTO.getRejectReason()),
                                WorkerCertificationAudit::getRejectReason,
                                certificationAuditReqDTO.getRejectReason());
        super.update(updateWrapper);

        WorkerCertificationUpdateDTO updateDTO = new WorkerCertificationUpdateDTO();
        updateDTO.setId(audit.getServeProviderId());
        updateDTO.setCertificationStatus(certificationAuditReqDTO.getCertificationStatus());
        if (ObjectUtil.equal(CertificationStatusEnum.SUCCESS.getStatus(),
                certificationAuditReqDTO.getCertificationStatus())) {
            serveProviderService.updateNameById(audit.getServeProviderId(), audit.getName());
            updateDTO.setName(audit.getName());
            updateDTO.setIdCardNo(audit.getIdCardNo());
            updateDTO.setFrontImg(audit.getFrontImg());
            updateDTO.setBackImg(audit.getBackImg());
            updateDTO.setCertificationMaterial(audit.getCertificationMaterial());
            updateDTO.setCertificationTime(LocalDateTime.now());
        }
        workerCertificationService.updateById(updateDTO);
    }

    @Override
    public PageResult<WorkerCertificationAuditResDTO> pageQuery(
            WorkerCertificationAuditPageQueryReqDTO queryReqDTO) {
        Page<WorkerCertificationAudit> page =
                PageUtils.parsePageQuery(queryReqDTO, WorkerCertificationAudit.class);
        LambdaQueryWrapper<WorkerCertificationAudit> queryWrapper =
                Wrappers.<WorkerCertificationAudit>lambdaQuery()
                        .like(ObjectUtil.isNotEmpty(queryReqDTO.getName()),
                                WorkerCertificationAudit::getName, queryReqDTO.getName())
                        .eq(ObjectUtil.isNotEmpty(queryReqDTO.getIdCardNo()),
                                WorkerCertificationAudit::getIdCardNo,
                                queryReqDTO.getIdCardNo())
                        .eq(ObjectUtil.isNotEmpty(queryReqDTO.getAuditStatus()),
                                WorkerCertificationAudit::getAuditStatus,
                                queryReqDTO.getAuditStatus())
                        .eq(ObjectUtil.isNotEmpty(queryReqDTO.getCertificationStatus()),
                                WorkerCertificationAudit::getCertificationStatus,
                                queryReqDTO.getCertificationStatus());
        return PageUtils.toPage(baseMapper.selectPage(page, queryWrapper),
                WorkerCertificationAuditResDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyCertification(WorkerCertificationAuditAddReqDTO addReqDTO) {
        WorkerCertificationAudit audit =
                BeanUtil.toBean(addReqDTO, WorkerCertificationAudit.class);
        audit.setAuditStatus(EnableStatusEnum.DISABLE.getStatus());
        audit.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
        baseMapper.insert(audit);

        Long serveProviderId = addReqDTO.getServeProviderId();
        WorkerCertification certification = workerCertificationService.getById(serveProviderId);
        if (certification == null) {
            certification = new WorkerCertification();
            certification.setId(serveProviderId);
            certification.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
            workerCertificationService.save(certification);
        } else {
            certification.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
            workerCertificationService.updateById(certification);
        }
    }

    @Override
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {
        LambdaQueryWrapper<WorkerCertificationAudit> queryWrapper =
                Wrappers.<WorkerCertificationAudit>lambdaQuery()
                        .eq(WorkerCertificationAudit::getServeProviderId,
                                UserContext.currentUserId())
                        .orderByDesc(WorkerCertificationAudit::getCreateTime)
                        .last("limit 1");
        WorkerCertificationAudit audit = baseMapper.selectOne(queryWrapper);
        return new RejectReasonResDTO(audit == null ? null : audit.getRejectReason());
    }
}
