package com.jzo2o.foundations.service;

import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;

import java.util.List;

/**
 * 首页查询相关功能
 *
 * @author itcast
 * @create 8/21 10:55
 **/
public interface HomeService {

    /**
     * 根据区域id获取服务图标信息
     *
     * @param regionId 区域id
     * @return 服务图标列表
     */
    List<ServeCategoryResDTO> queryServeIconCategoryByRegionIdCache(Long regionId);

    /**
     * 根据区域id查询服务类型列表
     *
     * @param regionId 区域id
     * @return 服务类型列表
     */
    List<ServeAggregationTypeSimpleResDTO> queryServeTypeListByRegionIdCache(Long regionId);

    /**
     * 根据区域id查询热门服务列表
     *
     * @param regionId 区域id
     * @return 热门服务列表
     */
    List<ServeAggregationSimpleResDTO> queryHotServeListByRegionIdCache(Long regionId);

    /**
     * 根据id查询服务详情
     *
     * @param id 服务id（serve表主键）
     * @return 服务详情
     */
    ServeAggregationSimpleResDTO queryServeByIdCache(Long id);
}
