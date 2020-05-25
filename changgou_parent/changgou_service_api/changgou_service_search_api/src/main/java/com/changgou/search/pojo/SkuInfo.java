package com.changgou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author zhouson
 * @create 2020-05-22 11:32
 * indexName 创建的索引库的名字
 * type 类型名称
 */

@Document(indexName = "skuinfo",type = "docs")
@Data
public class SkuInfo implements Serializable {
    /**
     * 商品id，同时也是商品编号
     */
    @Id
    private Long id;

    /**
     * type = FieldType.Text text类型支持分词
     * analyzer 创建索引时使用的分词器
     * store 是否存储(默认为false)
     * index 添加数据时是否分词（默认为true）
     * searchAnalyzer 搜索时使用的分词器
     * SKU名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String name;

    /**
     * 商品价格，单位为：元
     */
    @Field(type = FieldType.Double)
    private Long price;

    /**
     * 库存数量
     */
    private Integer num;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 商品状态，1-正常，2-下架，3-删除
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否默认
     */
    private String isDefault;

    /**
     * spu id
     */
    private Long spuId;

    /**
     * 类目id
     */
    private Long categoryId;

    /**
     * 类目名称
     * type = FieldType.Keyword keyword类型表示部分次
     */

    @Field(type = FieldType.Keyword)
    private String categoryName;

    /**
     * 品牌名称
     */
    @Field(type = FieldType.Keyword)
    private String brandName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 规格参数
     */
    private Map<String,Object> specMap;

}