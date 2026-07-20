package com.jzo2o.foundations.service;

import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;

import java.util.List;

/**
 * 服务搜索
 */
public interface IServeSearchService {

    /**
     * 根据城市编码、服务类型、关键字搜索服务
     *
     * @param cityCode    城市编码
     * @param serveTypeId 服务类型id
     * @param keyword     关键字
     * @return 服务简略列表
     */
    List<ServeSimpleResDTO> search(String cityCode, Long serveTypeId, String keyword);
}
