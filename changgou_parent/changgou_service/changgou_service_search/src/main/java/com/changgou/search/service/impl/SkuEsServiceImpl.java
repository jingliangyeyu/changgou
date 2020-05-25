package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuEsService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * @author zhouson
 * @create 2020-05-22 17:08
 */
@Service
public class SkuEsServiceImpl implements SkuEsService {
    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    /**
     * ElasticsearchTemplate是Spring对ES的java api进行的封装，提供了大量的相关的类来完成各种各样的查询
     */
    private ElasticsearchTemplate elasticsearchTemplate;

    /***
     * 导入SKU数据
     */
    @Override
    public void importSku() {
        //1.用feign来调用服务
        Result<List<Sku>> skuList = skuFeign.findByStatus("1");
        //2.将Result数据转换成List<SkuInfo>
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuList.getData()), SkuInfo.class);
        //3.实现动态域
        for (SkuInfo skuInfo : skuInfoList) {
            /**
             *  将spec字符串参数转化成map（就会产生多个key，也就是页面动态搜索条件）传入给specMap，
             *  然后会根据key产生不同的域，而key对应的value则会变成具体的搜索参数（类似于这种specMap.颜色）
             */
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec());
            skuInfo.setSpecMap(specMap);
        }
        //4.导入数据
        skuEsMapper.saveAll(skuInfoList);
    }

    /***
     * 搜索    这里采用map是因为前台传过来的搜索对象-关键词可能不只一个
     * 返回数据是map因为里面有查询出来的数据，也有其他数据(如分页数)
     * ctrl+alt+b 找出实现类
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //构建基础查询对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = getNativeSearchQueryBuilder(searchMap);
        //调用搜索查询
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);
        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);
        resultMap.putAll(groupMap);
        return resultMap;
    }

    /**
     * 得到分组数据集合
     * @param stringTerms
     * @return
     */
    private List<String> getGroupList(StringTerms stringTerms) {
        List<String> groupList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String filedName = bucket.getKeyAsString();
            groupList.add(filedName);
        }
        return groupList;
    }

    /**
     * 构建基本查询
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder getNativeSearchQueryBuilder(Map<String, String> searchMap) {
        // 查询构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //创建组合条件构建器
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        //当用户在上面搜索了条件时，别的条件也应该随着这而查询，比如选择华为，那么分类就需要变化，这些条件应该是合并条件
        if (searchMap != null) {
            //1.关键字查询
            if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
//                nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(searchMap.get("keywords")).field("name"));
                bool.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")));
            }
            //2.分类搜索 termQuery是不会进行分词（分类是不需要分词的）
            if (!StringUtils.isEmpty(searchMap.get("category"))) {
                bool.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }
            //3.品牌搜索termQuery是不会进行分词（品牌是不需要分词的）
            if (!StringUtils.isEmpty(searchMap.get("brand"))) {
                bool.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            //4.规格搜索，由于不知道前台传过来的是哪个具体规格，所以需要加上前缀（自己定，下面以spec_为例），注意不能分词
            for (String specKey : searchMap.keySet()) {
                //如果以前缀spec开头就是规格参数,域名参考es里面的域名(这里是specMap.规格名字)
                if (specKey.startsWith("spec_")) {
                    bool.must(QueryBuilders.termQuery("specMap." + specKey.substring(5) + ".keyword", searchMap.get(specKey)));
                }
            }
            //5.价格区间过滤
            if (!StringUtils.isEmpty(searchMap.get("price"))) {
                String[] prices = searchMap.get("price").split("-");
                //如果prices长度为2说明是一个区间值，长度为1说明直接大于这个值
                bool.must(QueryBuilders.rangeQuery("price").gt(prices[0]));
                if (prices.length == 2) {
                    bool.must(QueryBuilders.rangeQuery("price").lt(prices[1]));
                }
                serachSort(searchMap, nativeSearchQueryBuilder);
            }
            /**
             * 分页查询
             * 页面需要实现分页搜索，所以我们后台每次查询的时候，需要实现分页。用户页面每次会传入当前页和每页查询多少条数据，
             * 当然如果不传入每页显示多少条数据，默认查询30条即可
             */
            //获取当前页
            Integer pageNum = getPageNum(searchMap);
            //获取每页查询数量
            Integer pageSize = 3;
            //withPageable 分页查询，分页下标是从0开始的
            nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, pageSize));
            serachSort(searchMap, nativeSearchQueryBuilder);
        } else {
            searchMap.put("keywords", "");
        }
        //将BoolQueryBuilder对象填充给nativeSearchQueryBuilder对象
        nativeSearchQueryBuilder.withQuery(bool);
        return nativeSearchQueryBuilder;
    }

    /**
     * 排序查询
     * @param searchMap
     * @param nativeSearchQueryBuilder
     */
    private void serachSort(Map<String, String> searchMap, NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //排序查询 需要在后台接收2个参数，分别是排序域名字（sortField）和排序方式(sortRule),以价格为例
        //排序域名字
        String sortField = searchMap.get("sortField");
        //排序域规则
        String sortRule = searchMap.get("sortRule");
        if(!StringUtils.isEmpty(sortField)&&!StringUtils.isEmpty(sortRule)){
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
        }
    }

    /***
     * 数据搜索
     * @param builder
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder builder) {
        //高亮配置 指定高亮域
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        //前缀
        field.preTags("<em style=\"color:red;\">");
        //后缀
        field.postTags("</em>");
        //碎片长度 关键字长度
        field.fragmentSize(100);
        //添加高亮
        builder.withHighlightFields(field);


        //用于封装数据并放回
        Map<String, Object> resultMap = new HashMap<>();
        //执行查询条件
        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(
                builder.build(),  //搜索条件封装
                SkuInfo.class,    //数据集合要转换的类型的字节码
                //SearchResultMapper   执行搜索后，将数据集封装到该对象中
                new SearchResultMapper(

                ) {
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                        //用来存储所有转换后的高亮数据对象
                        List<T> list = new ArrayList<>();
                        //获取所有数据【包括高亮和非高亮数据】
                        for (SearchHit hit : response.getHits()) {
                            //分析结果集数据，获取非高亮数据，并转成SkuInfo对象
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            //分析结果集数据，获取指定域高亮数据
                            HighlightField name = hit.getHighlightFields().get("name");
                            if(name!=null && name.getFragments()!=null){
                                //读取高亮数据
                                StringBuffer buffer = new StringBuffer();
                                Text[] fragments = name.getFragments();
                                for (Text fragment : fragments) {
                                    buffer.append(fragment.toString());
                                }
                                //将非高亮数据替换成高亮数据
                                skuInfo.setName(buffer.toString());
                            }
                            //将高亮数据添加到集合中
                            list.add((T) skuInfo);
                        }
                        /**
                         * 返回数据
                         * 第一个参数：搜索的集合数据：携带高亮
                         * 第二个参数：分页对象信息
                         * 第三个参数：搜索的总记录数
                         */
                        return new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits());
                    }
                });
        //存储对应数据
        resultMap.put("totalElements", skuPage.getTotalElements());
        resultMap.put("totalPages", skuPage.getTotalPages());
        resultMap.put("rows", skuPage.getContent());
        return resultMap;
    }


    /***
     * 搜索分类分组数据
     * @param builder
     */
    public Map<String,Object> searchGroupList(NativeSearchQueryBuilder builder,Map<String,String> searchMap) {
        /***
         * 指定分类域，并根据分类域配置聚合查询
         * 1:给分组查询取别名
         * 2:指定分组查询的域
         */
        //定义一个map存取所有分组数据 判断条件表示用户选择分类作为条件，则不需要对分类进行搜索
        Map<String,Object> groupMapResult = new HashMap();
        //分类搜索
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        //品牌搜索
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            builder.addAggregation(AggregationBuilders.terms("skuBrandName").field("brandName"));
        }
        //规格搜索
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        //执行搜索
        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获取指定分组查询的数据
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = skuPage.getAggregations().get("skuCategory");
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList",categoryList);
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = skuPage.getAggregations().get("skuBrandName");
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList",brandList);
        }
        StringTerms specTerms = skuPage.getAggregations().get("skuSpec");
        //从所有数据中获取别名为skuCategory的数据
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> specMap = specPutAll(specList);
        groupMapResult.put("specList",specMap);

        return groupMapResult;
    }

    /**
     * 获取当前页
     */
    public static Integer getPageNum(Map<String, String> searchMap) {
        //  如果报异常默认设置当前页为1
        try {
            return Integer.parseInt(searchMap.get("pageNum"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 1;
    }
    /***
     * 将所有规格数据转入到Map中
     * @param specList
     * @return
     */
    public Map<String,Set<String>> specPutAll(List<String> specList){
        //新建一个Map
        Map<String,Set<String>> specMap = new HashMap<String,Set<String>>();
        //将集合数据存入到Map中
        for (String specString : specList) {
            //将Map数据转成Map
            Map<String,String> map = JSON.parseObject(specString, Map.class);

            //循环转换后的Map
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();        //规格名字
                String value = entry.getValue();    //规格选项值
                //获取当前规格名字对应的规格数据
                Set<String> specValues = specMap.get(key);
                if(specValues==null){
                    specValues = new HashSet<String>();
                }
                //将当前规格加入到集合中
                specValues.add(value);
                //将数据存入到specMap中
                specMap.put(key,specValues);
            }
        }
        return  specMap;
    }
    /**
     * 详细构建字符查询（供参考）
     *
     * @param searchMap
     * @return
     */
    private Map detailed(Map<String, String> searchMap) {
        //1.构建条件(SearchQuery)
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        NativeSearchQuery searchQuery = null;
        //1.1判断searchMap为空
        if (searchMap.size() > 0 && searchMap != null) {
            //假设前台传过来的搜索词也就是关键词key为keywords
            searchQuery = nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(searchMap.get("keywords")).field("name")).build();
        }
        //2.执行搜索，并返回结果
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(searchQuery, SkuInfo.class);
        //3.找出自己想要的结果
//        总页数
        int totalPages = skuInfos.getTotalPages();
//        总记录数
        long totalElements = skuInfos.getTotalElements();
//        数据结果集
        List<SkuInfo> content = skuInfos.getContent();
        //4.用map存储数据并返回
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalPages", totalPages);
        resultMap.put("totalElement", totalElements);
        //前台规定的返回参数rows
        resultMap.put("rows", content);
        return resultMap;
    }
}
