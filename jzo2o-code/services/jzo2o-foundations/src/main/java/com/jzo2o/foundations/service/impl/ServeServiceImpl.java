package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.foundations.service.IServeSyncService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class ServeServiceImpl extends ServiceImpl<ServeMapper, Serve> implements IServeService {

    @Resource
    private ServeItemMapper serveItemMapper;

    @Resource
    private RegionMapper regionMapper;

    @Resource
    private IServeSyncService serveSyncService;

    /**
     * 分页查询服务列表
     */
    @Override
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        //调用mapper查询数据，这里由于继承了ServiceImpl<ServeMapper, Serve>，使用baseMapper相当于使用ServeMapper
        PageResult<ServeResDTO> serveResDTOPageResult = PageHelperUtils.selectPage(servePageQueryReqDTO, () -> baseMapper.queryServeListByRegionId(servePageQueryReqDTO.getRegionId()));
        return serveResDTOPageResult;
    }

    /**
     * 批量新增
     *
     * @param serveUpsertReqDTOList 批量新增数据
     */
    @Override
    @Transactional
    public void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList) {
        for (ServeUpsertReqDTO serveUpsertReqDTO : serveUpsertReqDTOList) {
            //1.校验服务项是否为启用状态，不是启用状态不能新增
            ServeItem serveItem = serveItemMapper.selectById(serveUpsertReqDTO.getServeItemId());
            //如果服务项信息不存在或未启用
            if (ObjectUtil.isNull(serveItem) || serveItem.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
                throw new ForbiddenOperationException("该服务未启用无法添加到区域下使用");
            }

            //2.校验是否重复新增
            Long count = lambdaQuery()
                    .eq(Serve::getRegionId, serveUpsertReqDTO.getRegionId())
                    .eq(Serve::getServeItemId, serveUpsertReqDTO.getServeItemId())
                    .count();
            if (count > 0) {
                throw new ForbiddenOperationException(serveItem.getName() + "服务已存在");
            }

            //3.新增服务
            Serve serve = BeanUtil.toBean(serveUpsertReqDTO, Serve.class);
            //默认为服务项的价格
            serve.setPrice(serveItem.getReferencePrice());
            Region region = regionMapper.selectById(serveUpsertReqDTO.getRegionId());
            serve.setCityCode(region.getCityCode());
            baseMapper.insert(serve);
        }
    }

    /**
     * 服务价格修改
     *
     * @param id    服务id
     * @param price 价格
     * @return 服务
     */
    @Override
    @Transactional
    @CachePut(value = RedisConstants.CacheName.SERVE, key = "#id",
            cacheManager = RedisConstants.CacheManager.ONE_DAY)
    public Serve update(Long id, BigDecimal price) {
        //1.更新服务价格
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getPrice, price)
                .update();
        if (!update) {
            throw new CommonException("修改服务价格失败");
        }
        //2.同步到serve_sync，触发Canal写入ES
        serveSyncService.updatePrice(id, price);
        return baseMapper.selectById(id);
    }

    /**
     * 上架
     *
     * @param id 服务id
     */
    @Override
    @Transactional
    @CachePut(value = RedisConstants.CacheName.SERVE, key = "#id",
            cacheManager = RedisConstants.CacheManager.ONE_DAY)
    public Serve onSale(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("区域服务不存在");
        }
        //上架状态
        Integer saleStatus = serve.getSaleStatus();
        //草稿或下架状态方可上架
        if (!(saleStatus == FoundationStatusEnum.INIT.getStatus() || saleStatus == FoundationStatusEnum.DISABLE.getStatus())) {
            throw new ForbiddenOperationException("草稿或下架状态方可上架");
        }
        //服务项id
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        if (ObjectUtil.isNull(serveItem)) {
            throw new ForbiddenOperationException("所属服务项不存在");
        }
        //服务项的启用状态
        Integer activeStatus = serveItem.getActiveStatus();
        //服务项为启用状态方可上架
        if (!(FoundationStatusEnum.ENABLE.getStatus() == activeStatus)) {
            throw new ForbiddenOperationException("服务项为启用状态方可上架");
        }

        //更新上架状态
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus())
                .update();
        if (!update) {
            throw new CommonException("启动服务失败");
        }
        Serve dbServe = baseMapper.selectById(id);
        //写入serve_sync，触发Canal新增ES索引
        serveSyncService.batchInsertServeSync(Collections.singletonList(dbServe));
        return dbServe;
    }

    /**
     * 下架
     *
     * @param id 服务id
     */
    @Override
    @Transactional
    @CacheEvict(value = RedisConstants.CacheName.SERVE, key = "#id")
    public Serve offSale(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("区域服务不存在");
        }
        //售卖状态
        Integer saleStatus = serve.getSaleStatus();
        //上架状态方可下架
        if (!(FoundationStatusEnum.ENABLE.getStatus() == saleStatus)) {
            throw new ForbiddenOperationException("上架状态方可下架");
        }

        //更新下架状态
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.DISABLE.getStatus())
                .update();
        if (!update) {
            throw new CommonException("下架服务失败");
        }
        //删除serve_sync，触发Canal删除ES索引
        serveSyncService.removeById(id);
        return baseMapper.selectById(id);
    }

    /**
     * 删除区域服务
     *
     * @param id 服务id
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("区域服务不存在");
        }
        //草稿状态方可删除
        Integer saleStatus = serve.getSaleStatus();
        if (!(FoundationStatusEnum.INIT.getStatus() == saleStatus)) {
            throw new ForbiddenOperationException("草稿状态方可删除");
        }
        baseMapper.deleteById(id);
    }

    /**
     * 设置热门
     *
     * @param id 服务id
     */
    @Override
    @Transactional
    @CacheEvict(value = RedisConstants.CacheName.HOT_SERVE, key = "#result.regionId")
    public Serve onHot(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("区域服务不存在");
        }
        //更新为热门
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getIsHot, 1)
                .set(Serve::getHotTimeStamp, System.currentTimeMillis())
                .update();
        if (!update) {
            throw new CommonException("设置热门失败");
        }
        //同步到serve_sync
        serveSyncService.onHotSync(id);
        return baseMapper.selectById(id);
    }

    /**
     * 取消热门
     *
     * @param id 服务id
     */
    @Override
    @Transactional
    @CacheEvict(value = RedisConstants.CacheName.HOT_SERVE, key = "#result.regionId")
    public Serve offHot(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("区域服务不存在");
        }
        //取消热门
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getIsHot, 0)
                .set(Serve::getHotTimeStamp, null)
                .update();
        if (!update) {
            throw new CommonException("取消热门失败");
        }
        //同步到serve_sync
        serveSyncService.offHotSync(id);
        return baseMapper.selectById(id);
    }

    /**
     * 根据区域id和售卖状态查询服务数量
     *
     * @param regionId   区域id
     * @param saleStatus 售卖状态
     * @return 服务数量
     */
    @Override
    public int queryServeCountByRegionIdAndSaleStatus(Long regionId, Integer saleStatus) {
        return lambdaQuery()
                .eq(Serve::getRegionId, regionId)
                .eq(Serve::getSaleStatus, saleStatus)
                .count()
                .intValue();
    }

    /**
     * 根据服务项id和售卖状态查询服务数量
     *
     * @param serveItemId 服务项id
     * @param saleStatus  售卖状态
     * @return 服务数量
     */
    @Override
    public int queryServeCountByServeItemIdAndSaleStatus(Long serveItemId, Integer saleStatus) {
        return lambdaQuery()
                .eq(Serve::getServeItemId, serveItemId)
                .eq(Serve::getSaleStatus, saleStatus)
                .count()
                .intValue();
    }

    /**
     * 查询区域服务信息并进行缓存
     *
     * @param id 对应serve表的主键
     * @return 区域服务信息
     */
    @Override
    @Cacheable(value = RedisConstants.CacheName.SERVE, key = "#id",
            cacheManager = RedisConstants.CacheManager.ONE_DAY)
    public Serve queryServeByIdCache(Long id) {
        return getById(id);
    }
}
