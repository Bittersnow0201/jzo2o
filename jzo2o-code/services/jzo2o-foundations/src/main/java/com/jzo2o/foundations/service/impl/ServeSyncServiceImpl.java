package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.foundations.constants.IndexConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.mapper.ServeSyncMapper;
import com.jzo2o.foundations.mapper.ServeTypeMapper;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.domain.ServeSync;
import com.jzo2o.foundations.model.domain.ServeType;
import com.jzo2o.foundations.model.dto.request.ServeSyncUpdateReqDTO;
import com.jzo2o.foundations.service.IServeSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务同步表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
@Slf4j
@Service
public class ServeSyncServiceImpl extends ServiceImpl<ServeSyncMapper, ServeSync> implements IServeSyncService {

    @Resource
    private ServeItemMapper serveItemMapper;

    @Resource
    private ServeTypeMapper serveTypeMapper;

    @Resource
    private ServeMapper serveMapper;

    @Resource
    private ElasticSearchTemplate elasticSearchTemplate;

    /**
     * 根据服务项id更新
     *
     * @param serveItemId           服务项id
     * @param serveSyncUpdateReqDTO 服务同步更新数据
     */
    @Override
    public void updateByServeItemId(Long serveItemId, ServeSyncUpdateReqDTO serveSyncUpdateReqDTO) {
        LambdaUpdateWrapper<ServeSync> updateWrapper = Wrappers.<ServeSync>lambdaUpdate()
                .eq(ServeSync::getServeItemId, serveItemId)
                .set(ServeSync::getServeItemName, serveSyncUpdateReqDTO.getServeItemName())
                .set(ServeSync::getServeItemSortNum, serveSyncUpdateReqDTO.getServeItemSortNum())
                .set(ServeSync::getUnit, serveSyncUpdateReqDTO.getUnit())
                .set(ServeSync::getServeItemImg, serveSyncUpdateReqDTO.getServeItemImg())
                .set(ServeSync::getServeItemIcon, serveSyncUpdateReqDTO.getServeItemIcon());
        super.update(updateWrapper);
    }

    /**
     * 根据服务类型id更新
     *
     * @param serveTypeId           服务类型id
     * @param serveSyncUpdateReqDTO 服务同步更新数据
     */
    @Override
    public void updateByServeTypeId(Long serveTypeId, ServeSyncUpdateReqDTO serveSyncUpdateReqDTO) {
        LambdaUpdateWrapper<ServeSync> updateWrapper = Wrappers.<ServeSync>lambdaUpdate()
                .eq(ServeSync::getServeTypeId, serveTypeId)
                .set(ServeSync::getServeTypeName, serveSyncUpdateReqDTO.getServeTypeName())
                .set(ServeSync::getServeTypeImg, serveSyncUpdateReqDTO.getServeTypeImg())
                .set(ServeSync::getServeTypeIcon, serveSyncUpdateReqDTO.getServeTypeIcon())
                .set(ServeSync::getServeTypeSortNum, serveSyncUpdateReqDTO.getServeTypeSortNum());
        super.update(updateWrapper);
    }

    /**
     * 修改服务价格同步
     *
     * @param id    服务id
     * @param price 价格
     */
    @Override
    public void updatePrice(Long id, BigDecimal price) {
        ServeSync serveSync = new ServeSync();
        serveSync.setId(id);
        serveSync.setPrice(price);
        updateById(serveSync);
    }

    /**
     * 设置热门同步
     *
     * @param id 服务id
     */
    @Override
    public void onHotSync(Long id) {
        ServeSync serveSync = new ServeSync();
        serveSync.setId(id);
        serveSync.setIsHot(1);
        serveSync.setHotTimeStamp(System.currentTimeMillis());
        updateById(serveSync);
    }

    /**
     * 取消热门同步
     *
     * @param id 服务id
     */
    @Override
    public void offHotSync(Long id) {
        lambdaUpdate()
                .eq(ServeSync::getId, id)
                .set(ServeSync::getIsHot, 0)
                .set(ServeSync::getHotTimeStamp, null)
                .update();
    }

    /**
     * 批量新增同步记录（服务上架）
     * serve -> serve_sync -> binlog -> canal -> mq -> es
     *
     * @param list 区域服务列表
     */
    @Transactional
    @Override
    public void batchInsertServeSync(List<Serve> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Long> itemIds = list.stream().map(Serve::getServeItemId).collect(Collectors.toSet());
        List<ServeItem> serveItems = serveItemMapper.selectBatchIds(itemIds);
        Map<Long, ServeItem> itemMaps = serveItems.stream()
                .collect(Collectors.toMap(ServeItem::getId, serveItem -> serveItem));

        Set<Long> typeIds = serveItems.stream().map(ServeItem::getServeTypeId).collect(Collectors.toSet());
        List<ServeType> serveTypes = serveTypeMapper.selectBatchIds(typeIds);
        Map<Long, ServeType> typeMaps = serveTypes.stream()
                .collect(Collectors.toMap(ServeType::getId, serveType -> serveType));

        List<ServeSync> serveSyncs = list.stream().map(s -> {
            ServeSync serveSync = BeanUtil.toBean(s, ServeSync.class);

            ServeItem serveItem = itemMaps.get(s.getServeItemId());
            serveSync.setServeTypeId(serveItem.getServeTypeId());
            serveSync.setServeItemSortNum(serveItem.getSortNum());
            serveSync.setUnit(serveItem.getUnit());
            serveSync.setDetailImg(serveItem.getDetailImg());
            serveSync.setServeItemImg(serveItem.getImg());
            serveSync.setServeItemIcon(serveItem.getServeItemIcon());
            serveSync.setServeItemName(serveItem.getName());

            ServeType serveType = typeMaps.get(serveItem.getServeTypeId());
            serveSync.setServeTypeSortNum(serveType.getSortNum());
            serveSync.setServeTypeName(serveType.getName());
            serveSync.setServeTypeImg(serveType.getImg());
            serveSync.setServeTypeIcon(serveType.getServeTypeIcon());
            return serveSync;
        }).collect(Collectors.toList());

        saveBatch(serveSyncs);
    }

    /**
     * 修复缺失的 serve_sync，并全量同步到 ES
     */
    @Override
    @Transactional
    public int syncServeToEs() {
        // 1. 已上架但未写入 serve_sync 的数据补齐
        List<Serve> onSaleServes = serveMapper.selectList(Wrappers.<Serve>lambdaQuery()
                .eq(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus()));
        if (CollUtil.isNotEmpty(onSaleServes)) {
            Set<Long> syncIds = list().stream().map(ServeSync::getId).collect(Collectors.toSet());
            List<Serve> missing = onSaleServes.stream()
                    .filter(s -> !syncIds.contains(s.getId()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(missing)) {
                log.info("补齐缺失 serve_sync 记录 {} 条", missing.size());
                batchInsertServeSync(missing);
            }
        }

        // 2. 全量写入 ES
        List<ServeSync> all = list();
        if (CollUtil.isEmpty(all)) {
            return 0;
        }
        Boolean success = elasticSearchTemplate.opsForDoc().batchUpsert(IndexConstants.SERVE, all);
        if (!Boolean.TRUE.equals(success)) {
            throw new RuntimeException("同步 ES 失败");
        }
        log.info("已同步 {} 条服务到 ES 索引 {}", all.size(), IndexConstants.SERVE);
        return all.size();
    }
}
