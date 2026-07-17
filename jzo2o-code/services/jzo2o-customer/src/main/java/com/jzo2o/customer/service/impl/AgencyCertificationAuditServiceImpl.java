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
import com.jzo2o.customer.mapper.AgencyCertificationAuditMapper;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.jzo2o.customer.model.dto.AgencyCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import com.jzo2o.customer.service.IAgencyCertificationService;
import com.jzo2o.customer.service.IServeProviderService;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 机构认证审核服务实现
 */
@Service
public class AgencyCertificationAuditServiceImpl
        extends ServiceImpl<AgencyCertificationAuditMapper, AgencyCertificationAudit>
        implements IAgencyCertificationAuditService {

    @Resource
    private IAgencyCertificationService agencyCertificationService;

    @Resource
    private IServeProviderService serveProviderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCertification(Long id, CertificationAuditReqDTO certificationAuditReqDTO) {
        AgencyCertificationAudit audit = baseMapper.selectById(id);
        if (audit == null) {
            throw new BadRequestException("认证申请不存在");
        }

        CurrentUserInfo currentUserInfo = UserContext.currentUser();
        LambdaUpdateWrapper<AgencyCertificationAudit> updateWrapper =
                Wrappers.<AgencyCertificationAudit>lambdaUpdate()
                        .eq(AgencyCertificationAudit::getId, id)
                        .set(AgencyCertificationAudit::getAuditStatus,
                                EnableStatusEnum.ENABLE.getStatus())
                        .set(AgencyCertificationAudit::getAuditorId, currentUserInfo.getId())
                        .set(AgencyCertificationAudit::getAuditorName, currentUserInfo.getName())
                        .set(AgencyCertificationAudit::getAuditTime, LocalDateTime.now())
                        .set(AgencyCertificationAudit::getCertificationStatus,
                                certificationAuditReqDTO.getCertificationStatus())
                        .set(ObjectUtil.isNotEmpty(certificationAuditReqDTO.getRejectReason()),
                                AgencyCertificationAudit::getRejectReason,
                                certificationAuditReqDTO.getRejectReason());
        super.update(updateWrapper);

        AgencyCertificationUpdateDTO updateDTO = new AgencyCertificationUpdateDTO();
        updateDTO.setId(audit.getServeProviderId());
        updateDTO.setCertificationStatus(certificationAuditReqDTO.getCertificationStatus());
        if (ObjectUtil.equal(CertificationStatusEnum.SUCCESS.getStatus(),
                certificationAuditReqDTO.getCertificationStatus())) {
            serveProviderService.updateNameById(audit.getServeProviderId(), audit.getName());
            updateDTO.setName(audit.getName());
            updateDTO.setIdNumber(audit.getIdNumber());
            updateDTO.setLegalPersonName(audit.getLegalPersonName());
            updateDTO.setLegalPersonIdCardNo(audit.getLegalPersonIdCardNo());
            updateDTO.setBusinessLicense(audit.getBusinessLicense());
            updateDTO.setCertificationTime(LocalDateTime.now());
        }
        agencyCertificationService.updateByServeProviderId(updateDTO);
    }

    @Override
    public PageResult<AgencyCertificationAuditResDTO> pageQuery(
            AgencyCertificationAuditPageQueryReqDTO queryReqDTO) {
        Page<AgencyCertificationAudit> page =
                PageUtils.parsePageQuery(queryReqDTO, AgencyCertificationAudit.class);
        LambdaQueryWrapper<AgencyCertificationAudit> queryWrapper =
                Wrappers.<AgencyCertificationAudit>lambdaQuery()
                        .like(ObjectUtil.isNotEmpty(queryReqDTO.getName()),
                                AgencyCertificationAudit::getName, queryReqDTO.getName())
                        .like(ObjectUtil.isNotEmpty(queryReqDTO.getLegalPersonName()),
                                AgencyCertificationAudit::getLegalPersonName,
                                queryReqDTO.getLegalPersonName())
                        .eq(ObjectUtil.isNotEmpty(queryReqDTO.getAuditStatus()),
                                AgencyCertificationAudit::getAuditStatus,
                                queryReqDTO.getAuditStatus())
                        .eq(ObjectUtil.isNotEmpty(queryReqDTO.getCertificationStatus()),
                                AgencyCertificationAudit::getCertificationStatus,
                                queryReqDTO.getCertificationStatus());
        return PageUtils.toPage(baseMapper.selectPage(page, queryWrapper),
                AgencyCertificationAuditResDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyCertification(AgencyCertificationAuditAddReqDTO addReqDTO) {
        AgencyCertificationAudit audit =
                BeanUtil.toBean(addReqDTO, AgencyCertificationAudit.class);
        audit.setAuditStatus(EnableStatusEnum.DISABLE.getStatus());
        audit.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
        baseMapper.insert(audit);

        Long serveProviderId = addReqDTO.getServeProviderId();
        AgencyCertification certification = agencyCertificationService.getById(serveProviderId);
        if (certification == null) {
            certification = new AgencyCertification();
            certification.setId(serveProviderId);
            certification.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
            agencyCertificationService.save(certification);
        } else {
            certification.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
            agencyCertificationService.updateById(certification);
        }
    }

    @Override
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {
        LambdaQueryWrapper<AgencyCertificationAudit> queryWrapper =
                Wrappers.<AgencyCertificationAudit>lambdaQuery()
                        .eq(AgencyCertificationAudit::getServeProviderId,
                                UserContext.currentUserId())
                        .orderByDesc(AgencyCertificationAudit::getCreateTime)
                        .last("limit 1");
        AgencyCertificationAudit audit = baseMapper.selectOne(queryWrapper);
        return new RejectReasonResDTO(audit == null ? null : audit.getRejectReason());
    }
}
