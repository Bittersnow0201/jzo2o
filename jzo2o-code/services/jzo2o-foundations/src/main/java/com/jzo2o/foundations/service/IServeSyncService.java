package com.jzo2o.foundations.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeSync;
import com.jzo2o.foundations.model.dto.request.ServeSyncUpdateReqDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 服务同步表 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
public interface IServeSyncService extends IService<ServeSync> {
    /**
     * 根据服务项id更新
     *
     * @param serveItemId           服务项id
     * @param serveSyncUpdateReqDTO 服务同步更新数据
     */
    void updateByServeItemId(Long serveItemId, ServeSyncUpdateReqDTO serveSyncUpdateReqDTO);

    /**
     * 根据服务类型id更新
     *
     * @param serveTypeId           服务类型id
     * @param serveSyncUpdateReqDTO 服务同步更新数据
     */
    void updateByServeTypeId(Long serveTypeId, ServeSyncUpdateReqDTO serveSyncUpdateReqDTO);

    /**
     * 修改服务价格同步
     *
     * @param id    服务id
     * @param price 价格
     */
    void updatePrice(Long id, BigDecimal price);

    /**
     * 设置热门同步
     *
     * @param id 服务id
     */
    void onHotSync(Long id);

    /**
     * 取消热门同步
     *
     * @param id 服务id
     */
    void offHotSync(Long id);

    /**
     * 批量新增同步记录（服务上架）
     *
     * @param list 区域服务列表
     */
    void batchInsertServeSync(List<Serve> list);

    /**
     * 修复缺失的 serve_sync，并全量同步到 ES（搜索索引补数）
     *
     * @return 同步到 ES 的文档数量
     */
    int syncServeToEs();
}
