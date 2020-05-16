package com.changgou.goods.service;

import com.changgou.goods.pojo.Brand;

import java.util.List;

/**
 * @author zhouson
 * @create 2020-05-14 23:53
 */
public interface BrandService {
    /**
     * 查询所有品牌
     */
    List<Brand> findAll();

    /**
     * 根据id查询品牌
     * @return
     */
    Brand findById(int id);

    /**
     * 增加品牌
     * @return
     */
    int add(Brand brand);

    /**
     * 修改品牌
     */
    int update(Brand brand);

    /**
     * 删除品牌
     */
    int delete(int id);
}
