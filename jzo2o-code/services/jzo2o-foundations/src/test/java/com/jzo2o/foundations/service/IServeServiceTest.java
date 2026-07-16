package com.jzo2o.foundations.service;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.response.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;

@SpringBootTest
@Slf4j
class IServeServiceTest {
    @Resource
    private IServeService serveService;

    //分页测试
    @Test
    public void test_page(){
        ServePageQueryReqDTO servePageQueryReqDTO = new ServePageQueryReqDTO();
        servePageQueryReqDTO.setRegionId(1686303222843662337L);
        servePageQueryReqDTO.setPageNo(1L);
        servePageQueryReqDTO.setPageSize(3L);
        PageResult<ServeResDTO> page = serveService.page(servePageQueryReqDTO);
        log.info("page : {}", page);
        Assert.notEmpty(page.getList(),"列表为空");
    }

    //价格修改测试（请替换为库中真实存在的 serve.id）
    @Test
    public void test_update() {
        Serve serve = serveService.update(1693543106233835521L, BigDecimal.valueOf(38.3));
        log.info("serve : {}", serve);
        Assert.notNull(serve, "服务为空");
        Assert.isTrue(serve.getPrice().compareTo(BigDecimal.valueOf(38.3)) == 0, "价格修改失败");
    }

    //上架测试（请替换为库中草稿或下架状态的 serve.id）
    @Test
    public void test_onSale() {
        Serve serve = serveService.onSale(1693543106233835521L);
        log.info("serve : {}", serve);
        Assert.notNull(serve, "服务为空");
        Assert.isTrue(FoundationStatusEnum.ENABLE.getStatus() == serve.getSaleStatus(), "上架失败");
    }

    //下架测试（请替换为库中上架状态的 serve.id）
    @Test
    public void test_offSale() {
        Serve serve = serveService.offSale(1693543106233835521L);
        log.info("serve : {}", serve);
        Assert.notNull(serve, "服务为空");
        Assert.isTrue(FoundationStatusEnum.DISABLE.getStatus() == serve.getSaleStatus(), "下架失败");
    }

    //删除测试（请替换为库中草稿状态的 serve.id）
    @Test
    public void test_deleteById() {
        serveService.deleteById(1693543106233835521L);
    }

    //设置热门测试（请替换为库中真实存在的 serve.id）
    @Test
    public void test_onHot() {
        Serve serve = serveService.onHot(1693543106233835521L);
        log.info("serve : {}", serve);
        Assert.notNull(serve, "服务为空");
        Assert.isTrue(Integer.valueOf(1).equals(serve.getIsHot()), "设置热门失败");
    }

    //取消热门测试（请替换为库中热门状态的 serve.id）
    @Test
    public void test_offHot() {
        Serve serve = serveService.offHot(1693543106233835521L);
        log.info("serve : {}", serve);
        Assert.notNull(serve, "服务为空");
        Assert.isTrue(Integer.valueOf(0).equals(serve.getIsHot()), "取消热门失败");
    }
}
