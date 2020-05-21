package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.Page;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface SpuService {

    /***
     * 查询所有
     * @return
     */
    List<Spu> findAll();

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    Spu findById(String id);

    /***
     * 新增
     * @param goods
     */
    void add(Goods goods);

    /***
     * 修改
     * @param goods
     */
    void update(Goods goods);

    /***
     * 物理删除
     * @param spuId
     */
    void delete(Long spuId);

    /***
     * 逻辑删除
     * @param spuId
     */
    void logicDelete(Long spuId);

    /***
     * 还原删除（逻辑删除）数据
     * @param spuId
     */
    void restore(Long spuId);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<Spu> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(Map<String, Object> searchMap, int page, int size);

    /**
     * 保存-修改商品
     * @param goods
     * @throws ParseException
     */
    void saveGoods(Goods goods) throws ParseException;

    /**
     * 根据spuId查询商品详情
     * @param spuId
     * @return
     */
    Goods findGoodsBySpuId(Long spuId);

    /***
     * 商品审核（审核通过自动上架）
     * @param spuId
     */
    void audit(Long spuId);

    /**
     * 下架
     * @param spuId
     */
     void pull(Long spuId);

    /**
     * 批量下架
     * @param ids 需要下架的商品ID集合
     * @return
     */
    int pullMany(Long[] ids);

    /**
     * 上架
     * @param spuId
     */
    void put(Long spuId);

    /**
     * 批量上架
     * @param ids 需要上架的商品ID集合
     * @return
     */
    int putMany(Long[] ids);
}
