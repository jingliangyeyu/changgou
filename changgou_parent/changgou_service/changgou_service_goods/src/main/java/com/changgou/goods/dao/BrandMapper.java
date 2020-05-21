package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhouson
 * @create 2020-05-14 23:36
 */
public interface BrandMapper extends Mapper<Brand> {
    @Select("select b.name from tb_brand b,tb_category_brand tcb where tcb.category_id = #{categoryId} and b.id=tcb.brand_id")
    List<Brand> findByCategoryId(Integer categoryId);
}
