package com.jzo2o.foundations.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import com.jzo2o.foundations.service.IServeItemService;
import com.jzo2o.foundations.service.IServeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 首页查询相关功能
 *
 * @author itcast
 * @create 8/21 10:57
 **/
@Slf4j
@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private IRegionService regionService;
    @Resource
    private ServeMapper serveMapper;
    @Resource
    private IServeService serveService;
    @Resource
    private IServeItemService serveItemService;

    /**
     * 根据区域id查询已开通的服务类型
     *
     * @param regionId 区域id
     * @return 已开通的服务类型
     */
    @Override
    @Caching(
            cacheable = {
                    // result为空时,属于缓存穿透情况，缓存时间30分钟
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId",
                            unless = "#result.size() != 0",
                            cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    // result不为空时,永久缓存
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId",
                            unless = "#result.size() == 0",
                            cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeCategoryResDTO> queryServeIconCategoryByRegionIdCache(Long regionId) {
        //1.校验当前城市是否为启用状态
        Region region = regionService.getById(regionId);
        if (ObjectUtil.isEmpty(region) || ObjectUtil.equal(FoundationStatusEnum.DISABLE.getStatus(), region.getActiveStatus())) {
            return Collections.emptyList();
        }

        //2.根据区域id查询所有的服务图标
        List<ServeCategoryResDTO> list = serveMapper.findServeIconCategoryByRegionId(regionId);
        if (ObjectUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        //3.服务类型取前两个，每个类型下服务项取前4个
        int endIndex = list.size() >= 2 ? 2 : list.size();
        List<ServeCategoryResDTO> serveCategoryResDTOS = new ArrayList<>(list.subList(0, endIndex));
        serveCategoryResDTOS.forEach(v -> {
            List<ServeSimpleResDTO> serveResDTOList = v.getServeResDTOList();
            if (ObjectUtil.isEmpty(serveResDTOList)) {
                return;
            }
            int endIndex2 = serveResDTOList.size() >= 4 ? 4 : serveResDTOList.size();
            List<ServeSimpleResDTO> serveSimpleResDTOS = new ArrayList<>(serveResDTOList.subList(0, endIndex2));
            v.setServeResDTOList(serveSimpleResDTOS);
        });

        return serveCategoryResDTOS;
    }

    /**
     * 根据区域id查询服务类型列表
     *
     * @param regionId 区域id
     * @return 服务类型列表
     */
    @Override
    @Caching(
            cacheable = {
                    // 空列表缓存30分钟，防止缓存穿透
                    @Cacheable(value = RedisConstants.CacheName.SERVE_TYPE, key = "#regionId",
                            unless = "#result.size() != 0",
                            cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    // 有数据则永久缓存
                    @Cacheable(value = RedisConstants.CacheName.SERVE_TYPE, key = "#regionId",
                            unless = "#result.size() == 0",
                            cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeAggregationTypeSimpleResDTO> queryServeTypeListByRegionIdCache(Long regionId) {
        Region region = regionService.getById(regionId);
        if (ObjectUtil.isEmpty(region) || ObjectUtil.equal(FoundationStatusEnum.DISABLE.getStatus(), region.getActiveStatus())) {
            return Collections.emptyList();
        }
        List<ServeAggregationTypeSimpleResDTO> list = serveMapper.findServeTypeListByRegionId(regionId);
        return ObjectUtil.isEmpty(list) ? Collections.emptyList() : list;
    }

    /**
     * 根据区域id查询热门服务列表
     *
     * @param regionId 区域id
     * @return 热门服务列表
     */
    @Override
    @Caching(
            cacheable = {
                    @Cacheable(value = RedisConstants.CacheName.HOT_SERVE, key = "#regionId",
                            unless = "#result.size() != 0",
                            cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    @Cacheable(value = RedisConstants.CacheName.HOT_SERVE, key = "#regionId",
                            unless = "#result.size() == 0",
                            cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeAggregationSimpleResDTO> queryHotServeListByRegionIdCache(Long regionId) {
        Region region = regionService.getById(regionId);
        if (ObjectUtil.isEmpty(region) || ObjectUtil.equal(FoundationStatusEnum.DISABLE.getStatus(), region.getActiveStatus())) {
            return Collections.emptyList();
        }
        List<ServeAggregationSimpleResDTO> list = serveMapper.findHotServeListByRegionId(regionId);
        return ObjectUtil.isEmpty(list) ? Collections.emptyList() : list;
    }

    /**
     * 根据id查询服务详情（拼装服务信息 + 服务项信息）
     *
     * @param id 服务id（serve表主键）
     * @return 服务详情
     */
    @Override
    public ServeAggregationSimpleResDTO queryServeByIdCache(Long id) {
        //1.查询服务信息（带缓存）
        Serve serve = serveService.queryServeByIdCache(id);
        if (ObjectUtil.isEmpty(serve)) {
            return null;
        }

        //2.查询服务项信息（带缓存）
        ServeItem serveItem = serveItemService.queryServeItemByIdCache(serve.getServeItemId());
        if (ObjectUtil.isEmpty(serveItem)) {
            return null;
        }

        //3.拼装返回
        return ServeAggregationSimpleResDTO.builder()
                .id(serve.getId())
                .serveItemId(serve.getServeItemId())
                .cityCode(serve.getCityCode())
                .price(serve.getPrice())
                .serveItemName(serveItem.getName())
                .serveItemImg(serveItem.getImg())
                .unit(serveItem.getUnit())
                .detailImg(serveItem.getDetailImg())
                .build();
    }
}
