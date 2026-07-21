package com.jzo2o.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface ICouponService extends IService<Coupon> {

    /**
     * 查询我的优惠券列表（滚动分页）
     *
     * @param lastId 上一页最后一条记录id，首页传 null
     * @param userId 用户id
     * @param status 优惠券状态，1：未使用，2：已使用，3：已过期
     * @return 优惠券列表
     */
    List<CouponInfoResDTO> queryForList(Long lastId, Long userId, Integer status);

    /**
     * 获取可用优惠券列表，并按优惠金额从大到小排序
     *
     * @param userId      用户id
     * @param totalAmount 订单总金额
     * @return 可用优惠券列表
     */
    List<AvailableCouponsResDTO> getAvailable(Long userId, BigDecimal totalAmount);

    /**
     * 核销优惠券，并返回优惠金额
     *
     * @param couponUseReqDTO 核销请求
     * @return 优惠金额
     */
    CouponUseResDTO use(CouponUseReqDTO couponUseReqDTO);

    /**
     * 优惠券退回
     *
     * @param couponUseBackReqDTO 退回请求
     */
    void useBack(CouponUseBackReqDTO couponUseBackReqDTO);
}
