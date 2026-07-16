package com.jzo2o.foundations.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;

import java.math.BigDecimal;
import java.util.List;

public interface IServeService extends IService<Serve> {

    /**
     * 分页查询服务列表
     */
    PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO);

    /**
     * 批量新增
     *
     * @param serveUpsertReqDTOList 批量新增数据
     */
    void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList);

    /**
     * 服务价格修改
     *
     * @param id    服务id
     * @param price 价格
     * @return 服务
     */
    Serve update(Long id, BigDecimal price);

    /**
     * 上架
     *
     * @param id 服务id
     */
    Serve onSale(Long id);

    /**
     * 下架
     *
     * @param id 服务id
     */
    Serve offSale(Long id);

    /**
     * 删除区域服务
     *
     * @param id 服务id
     */
    void deleteById(Long id);

    /**
     * 设置热门
     *
     * @param id 服务id
     */
    Serve onHot(Long id);

    /**
     * 取消热门
     *
     * @param id 服务id
     */
    Serve offHot(Long id);

    /**
     * 根据区域id和售卖状态查询服务数量
     *
     * @param regionId   区域id
     * @param saleStatus 售卖状态
     * @return 服务数量
     */
    int queryServeCountByRegionIdAndSaleStatus(Long regionId, Integer saleStatus);

    /**
     * 根据服务项id和售卖状态查询服务数量
     *
     * @param serveItemId 服务项id
     * @param saleStatus  售卖状态
     * @return 服务数量
     */
    int queryServeCountByServeItemIdAndSaleStatus(Long serveItemId, Integer saleStatus);
}
