package com.jzo2o.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.market.model.domain.CouponIssue;
import com.jzo2o.market.model.dto.request.CouponIssueReqDTO;

import java.util.List;

/**
 * <p>
 * 发放优惠券服务类
 * </p>

 */
public interface ICouponIssueService extends IService<CouponIssue> {

    /**
     * 提交待发放优惠券数据
     *
     * @param couponIssueReqDTO 请求参数
     * @return 新增的待发放记录
     */
    List<CouponIssue> save(CouponIssueReqDTO couponIssueReqDTO);

    /**
     * 立即发放优惠券
     *
     * @param couponIssueReqDTO 请求参数
     * @return 发放记录
     */
    List<CouponIssue> issue(CouponIssueReqDTO couponIssueReqDTO);

    /**
     * 自动发放优惠券
     *
     * @param activityId 活动id
     */
    void autoIssue(Long activityId);
}
