package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.foundations.constants.IndexConstants;
import com.jzo2o.foundations.model.domain.ServeSync;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.IServeSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务搜索实现（基于 ES serve_aggregation）
 */
@Slf4j
@Service
public class ServeSearchServiceImpl implements IServeSearchService {

    @Resource
    private ElasticSearchTemplate elasticSearchTemplate;

    /**
     * 搜索实现
     * 条件：城市编码、关键字（服务项名称/服务类型名称）、服务类型；按服务项排序字段排序
     */
    @Override
    public List<ServeSimpleResDTO> search(String cityCode, Long serveTypeId, String keyword) {
        SearchRequest searchRequest = SearchRequest.of(builder -> builder
                .index(IndexConstants.SERVE)
                .query(queryBuilder -> {
                    queryBuilder.bool(boolBuilder -> {
                        // 关键字：模糊匹配服务项名称、服务类型名称
                        if (ObjectUtil.isNotEmpty(keyword)) {
                            boolBuilder.must(m -> m.bool(b -> b
                                    .should(s -> s.match(mm -> mm.field("serve_item_name").query(keyword)))
                                    .should(s -> s.match(mm -> mm.field("serve_type_name").query(keyword)))
                                    .minimumShouldMatch("1")
                            ));
                        } else {
                            boolBuilder.must(m -> m.matchAll(ma -> ma));
                        }

                        // 城市匹配
                        if (ObjectUtil.isNotEmpty(cityCode)) {
                            boolBuilder.filter(f -> f.term(t -> t.field("city_code").value(cityCode)));
                        }

                        // 服务分类匹配
                        if (ObjectUtil.isNotEmpty(serveTypeId)) {
                            boolBuilder.filter(f -> f.term(t -> t.field("serve_type_id").value(serveTypeId)));
                        }
                        return boolBuilder;
                    });
                    return queryBuilder;
                })
                // 按服务项排序字段升序
                .sort(s -> s.field(f -> f.field("serve_item_sort_num").order(SortOrder.Asc)))
        );

        SearchResponse<ServeSync> response = elasticSearchTemplate.opsForDoc().search(searchRequest, ServeSync.class);
        if (response == null || response.hits() == null || response.hits().hits() == null) {
            return Collections.emptyList();
        }

        return response.hits().hits().stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> BeanUtil.toBean(hit.source(), ServeSimpleResDTO.class))
                .collect(Collectors.toList());
    }
}
