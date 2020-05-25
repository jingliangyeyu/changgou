package com.changgou.search.dao;


import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author zhouson
 * @create 2020-05-22 17:04
 */
@Repository
/**
 * 第一个参数是导入的对象，第二个导入的对象主键id类型
 */
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {
}
