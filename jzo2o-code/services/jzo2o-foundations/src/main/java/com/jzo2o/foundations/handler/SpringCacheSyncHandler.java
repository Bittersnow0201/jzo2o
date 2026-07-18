package com.jzo2o.foundations.handler;

import cn.hutool.core.util.ObjectUtil;
import com.jzo2o.api.foundations.dto.response.RegionSimpleResDTO;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import com.jzo2o.foundations.service.IServeItemService;
import com.jzo2o.foundations.service.IServeService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * springCache缓存同步任务
 *
 * @author itcast
 * @create 8/15 18:14
 **/
@Slf4j
@Component
public class SpringCacheSyncHandler {

    @Resource
    private IRegionService regionService;
    @Resource
    private HomeService homeService;
    @Resource
    private IServeService serveService;
    @Resource
    private IServeItemService serveItemService;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 已启用区域缓存更新
     * 每日凌晨执行
     */
    @XxlJob("activeRegionCacheSync")
    public void activeRegionCacheSync() {
        log.info(">>>>>>>>开始进行缓存同步，更新已启用区域");

        //删除开通区域列表缓存
        redisTemplate.delete(RedisConstants.CacheName.JZ_CACHE + "::ACTIVE_REGIONS");

        //通过查询开通区域列表进行缓存
        List<RegionSimpleResDTO> regionSimpleResDTOS = regionService.queryActiveRegionList();

        //遍历区域对该区域下的首页服务列表进行缓存
        regionSimpleResDTOS.forEach(item -> {
            Long regionId = item.getId();

            //删除并刷新该区域下的首页服务列表
            String serveIconKey = RedisConstants.CacheName.SERVE_ICON + "::" + regionId;
            redisTemplate.delete(serveIconKey);
            homeService.queryServeIconCategoryByRegionIdCache(regionId);

            //删除并刷新该区域下的服务类型列表缓存
            String serveTypeKey = RedisConstants.CacheName.SERVE_TYPE + "::" + regionId;
            redisTemplate.delete(serveTypeKey);
            homeService.queryServeTypeListByRegionIdCache(regionId);

            //删除并刷新该区域下的热门服务列表缓存
            String hotServeKey = RedisConstants.CacheName.HOT_SERVE + "::" + regionId;
            redisTemplate.delete(hotServeKey);
            List<ServeAggregationSimpleResDTO> hotServeList = homeService.queryHotServeListByRegionIdCache(regionId);

            //热门服务的服务信息、服务项信息：先删缓存再查询缓存
            if (ObjectUtil.isNotEmpty(hotServeList)) {
                hotServeList.forEach(hotServe -> {
                    String serveKey = RedisConstants.CacheName.SERVE + "::" + hotServe.getId();
                    String serveItemKey = RedisConstants.CacheName.SERVE_ITEM + "::" + hotServe.getServeItemId();
                    redisTemplate.delete(serveKey);
                    redisTemplate.delete(serveItemKey);
                    serveService.queryServeByIdCache(hotServe.getId());
                    serveItemService.queryServeItemByIdCache(hotServe.getServeItemId());
                });
            }
        });

        log.info(">>>>>>>>更新已启用区域完成");
    }
}
