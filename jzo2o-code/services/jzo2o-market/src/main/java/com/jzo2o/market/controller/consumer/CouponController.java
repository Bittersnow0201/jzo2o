package com.jzo2o.market.controller.consumer;

import com.jzo2o.common.utils.UserContext;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端 - 优惠券相关接口
 */
@RestController("consumerCouponController")
@RequestMapping("/consumer/coupon")
@Api(tags = "用户端 - 优惠券相关接口")
public class CouponController {

    @Resource
    private ICouponService couponService;

    @GetMapping("/my")
    @ApiOperation("我的优惠券列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "优惠券状态，1：未使用，2：已使用，3：已过期", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "lastId", value = "上一次查询的最后一条记录的id", required = false, dataTypeClass = Long.class)
    })
    public List<CouponInfoResDTO> queryMyCouponList(@RequestParam("status") Integer status,
                                                    @RequestParam(value = "lastId", required = false) Long lastId) {
        return couponService.queryForList(lastId, UserContext.currentUserId(), status);
    }
}
