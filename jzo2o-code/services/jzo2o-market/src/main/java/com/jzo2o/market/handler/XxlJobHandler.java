package com.jzo2o.market.handler;

import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.redis.sync.SyncManager;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class XxlJobHandler {

    @Resource
    private SyncManager syncManager;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponService couponService;

    @Resource
    private IssuedCouponHandlerJob issuedCouponHandlerJob;

    /**
     * 活动状态修改，
     * 1.活动进行中状态修改
     * 2.活动已失效状态修改
     * 1分钟一次
     */
    @XxlJob("updateActivityStatus")
    public void updateActivitySatus(){

    }

    /**
     * 已领取优惠券自动过期任务
     */
    @XxlJob("processExpireCoupon")
    public void processExpireCoupon() {

    }

    /**
     * 自动发放优惠券
     */
    @XxlJob("issueCouponJob")
    public void issueCouponJob() {
        issuedCouponHandlerJob.start();
    }

}
