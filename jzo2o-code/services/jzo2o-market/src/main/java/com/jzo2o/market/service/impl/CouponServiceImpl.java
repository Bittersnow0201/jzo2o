package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.IdUtils;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.domain.CouponUseBack;
import com.jzo2o.market.model.domain.CouponWriteOff;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.market.utils.CouponUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    /**
     * 我的优惠券列表每页条数
     */
    private static final int PAGE_SIZE = 10;

    @Resource(name = "seizeCouponScript")
    private DefaultRedisScript<String> seizeCouponScript;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponUseBackService couponUseBackService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;

    @Override
    public List<CouponInfoResDTO> queryForList(Long lastId, Long userId, Integer status) {
        // 滚动分页：按雪花id降序（与领取时间降序一致），类似 search_after
        List<Coupon> coupons = lambdaQuery()
                .eq(Coupon::getUserId, userId)
                .eq(ObjectUtils.isNotNull(status), Coupon::getStatus, status)
                .lt(ObjectUtils.isNotNull(lastId), Coupon::getId, lastId)
                .orderByDesc(Coupon::getId)
                .last("limit " + PAGE_SIZE)
                .list();
        if (ObjectUtils.isEmpty(coupons)) {
            return Collections.emptyList();
        }
        return BeanUtils.copyToList(coupons, CouponInfoResDTO.class);
    }

    @Override
    public List<AvailableCouponsResDTO> getAvailable(Long userId, BigDecimal totalAmount) {
        if (userId == null || totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Collections.emptyList();
        }
        // 查询当前用户未使用且未过期、满足满减门槛的优惠券
        List<Coupon> coupons = lambdaQuery()
                .eq(Coupon::getUserId, userId)
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .gt(Coupon::getValidityTime, LocalDateTime.now())
                .le(Coupon::getAmountCondition, totalAmount)
                .list();
        if (ObjectUtils.isEmpty(coupons)) {
            return Collections.emptyList();
        }
        // 计算优惠金额，过滤优惠金额 >= 订单金额的，并按优惠金额降序
        return coupons.stream()
                .map(coupon -> {
                    AvailableCouponsResDTO dto = BeanUtils.copyBean(coupon, AvailableCouponsResDTO.class);
                    dto.setDiscountAmount(CouponUtils.calDiscountAmount(coupon, totalAmount));
                    return dto;
                })
                .filter(dto -> dto.getDiscountAmount() != null
                        && dto.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                        && dto.getDiscountAmount().compareTo(totalAmount) < 0)
                .sorted(Comparator.comparing(AvailableCouponsResDTO::getDiscountAmount).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUseResDTO use(CouponUseReqDTO couponUseReqDTO) {
        if (couponUseReqDTO == null || couponUseReqDTO.getId() == null
                || couponUseReqDTO.getOrdersId() == null || couponUseReqDTO.getTotalAmount() == null) {
            throw new CommonException("优惠券核销参数不完整");
        }
        Coupon coupon = getById(couponUseReqDTO.getId());
        if (coupon == null) {
            throw new CommonException("优惠券不存在");
        }
        // 未使用
        if (!CouponStatusEnum.NO_USE.equals(coupon.getStatus())) {
            throw new CommonException("优惠券不可用，当前状态：" + coupon.getStatus());
        }
        // 未过期
        if (coupon.getValidityTime() == null || coupon.getValidityTime().isBefore(LocalDateTime.now())) {
            throw new CommonException("优惠券已过期");
        }
        // 订单金额 >= 满减条件
        if (coupon.getAmountCondition() != null
                && couponUseReqDTO.getTotalAmount().compareTo(coupon.getAmountCondition()) < 0) {
            throw new CommonException("订单金额不满足优惠券使用条件");
        }
        BigDecimal discountAmount = CouponUtils.calDiscountAmount(coupon, couponUseReqDTO.getTotalAmount());
        if (discountAmount.compareTo(BigDecimal.ZERO) <= 0
                || discountAmount.compareTo(couponUseReqDTO.getTotalAmount()) >= 0) {
            throw new CommonException("优惠金额不合法");
        }
        LocalDateTime now = LocalDateTime.now();
        // 标记优惠券已使用
        boolean updated = lambdaUpdate()
                .set(Coupon::getStatus, CouponStatusEnum.USED.getStatus())
                .set(Coupon::getUseTime, now)
                .set(Coupon::getOrdersId, String.valueOf(couponUseReqDTO.getOrdersId()))
                .eq(Coupon::getId, coupon.getId())
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .update();
        if (!updated) {
            throw new CommonException("优惠券核销失败");
        }
        // 写入核销记录
        CouponWriteOff couponWriteOff = CouponWriteOff.builder()
                .id(IdUtils.getSnowflakeNextId())
                .couponId(coupon.getId())
                .userId(coupon.getUserId())
                .ordersId(couponUseReqDTO.getOrdersId())
                .activityId(coupon.getActivityId())
                .writeOffTime(now)
                .writeOffManName(coupon.getUserName())
                .writeOffManPhone(coupon.getUserPhone())
                .build();
        couponWriteOffService.save(couponWriteOff);

        CouponUseResDTO couponUseResDTO = new CouponUseResDTO();
        couponUseResDTO.setDiscountAmount(discountAmount);
        return couponUseResDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useBack(CouponUseBackReqDTO couponUseBackReqDTO) {
        if (couponUseBackReqDTO == null || couponUseBackReqDTO.getId() == null) {
            throw new CommonException("优惠券退回参数不完整");
        }
        Coupon coupon = getById(couponUseBackReqDTO.getId());
        if (coupon == null) {
            throw new CommonException("优惠券不存在");
        }
        // 查询核销记录
        CouponWriteOff couponWriteOff = couponWriteOffService.lambdaQuery()
                .eq(CouponWriteOff::getCouponId, couponUseBackReqDTO.getId())
                .eq(ObjectUtils.isNotNull(couponUseBackReqDTO.getOrdersId()),
                        CouponWriteOff::getOrdersId, couponUseBackReqDTO.getOrdersId())
                .one();

        LocalDateTime now = LocalDateTime.now();
        // 添加退回记录
        CouponUseBack couponUseBack = new CouponUseBack();
        couponUseBack.setId(IdUtils.getSnowflakeNextId());
        couponUseBack.setCouponId(coupon.getId());
        couponUseBack.setUserId(ObjectUtils.isNotNull(couponUseBackReqDTO.getUserId())
                ? couponUseBackReqDTO.getUserId() : coupon.getUserId());
        couponUseBack.setUseBackTime(now);
        couponUseBack.setWriteOffTime(couponWriteOff != null ? couponWriteOff.getWriteOffTime() : coupon.getUseTime());
        couponUseBackService.save(couponUseBack);

        // 已过期 → 作废；未过期 → 恢复未使用并清空订单信息
        if (coupon.getValidityTime() != null && coupon.getValidityTime().isBefore(now)) {
            lambdaUpdate()
                    .set(Coupon::getStatus, CouponStatusEnum.VOIDED.getStatus())
                    .eq(Coupon::getId, coupon.getId())
                    .update();
        } else {
            lambdaUpdate()
                    .set(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                    .set(Coupon::getOrdersId, null)
                    .set(Coupon::getUseTime, null)
                    .eq(Coupon::getId, coupon.getId())
                    .update();
        }
        // 删除核销记录
        if (couponWriteOff != null) {
            couponWriteOffService.removeById(couponWriteOff.getId());
        }
    }
}
