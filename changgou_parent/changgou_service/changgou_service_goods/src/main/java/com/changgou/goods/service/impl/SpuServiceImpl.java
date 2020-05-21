package com.changgou.goods.service.impl;


import com.alibaba.fastjson.JSON;
import com.changgou.util.IdWorker;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 添加商品
     * @param goods
     */
    @Override
    public void add(Goods goods) {

    }

    /**
     * 更新
     * @param goods
     */
    @Override
    public void update(Goods goods) {

    }

    /**
     * 删除
     * @param spuId
     */
    @Override
    public void delete(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否被逻辑删除  ,必须先逻辑删除后才能物理删除
        if(!spu.getIsDelete().equals("1")){
            throw new RuntimeException("此商品不能删除！");
        }
        spuMapper.deleteByPrimaryKey(spuId);
    }

    /**
     * 逻辑删除
     * @param spuId
     */
    @Override
    public void logicDelete(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否下架的商品
        if(!spu.getIsMarketable().equals("0")){
            throw new RuntimeException("必须先下架再删除！");
        }
        //删除
        spu.setIsDelete("1");
        //未审核
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 还原删除（逻辑删除）数据
     * @param spuId
     */
    @Override
    public void restore(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if(!spu.getIsDelete().equals("1")){
            throw new RuntimeException("该物品未删除");
        }
        spu.setIsDelete("0");
        //还原逻辑删除数据将审核改成未审核，此处可以省略书写
        //spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }
    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }

    /***
     * 保存-修改Goods
     * @param goods
     */

    @Override
    public void saveGoods(Goods goods) {
        IdWorker idWorker = new IdWorker();
        //增加Spu
        Spu spu = goods.getSpu();
        //如果前台传来的数据，spu的id不为空，说明为增加，反之则为修改
        if(spu.getId()==null){
            //为空说明是增加
            spu.setId(String.valueOf(idWorker.nextId()));
            spuMapper.insertSelective(spu);
        } else{
            //不为空说明为修改
            spuMapper.updateByPrimaryKeySelective(spu);
            //删掉spu下面的sku，再将sku增加，这样做的好处是不用管之前有什么操作，
            // 之后的修改操作有什么影响（可能对比上一次删掉了5G这个选项，添加了3G这个选项，管理起来就特别麻烦）
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            skuMapper.delete(sku);
        }

        //增加Sku
        Date date = new Date();
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        //获取Sku集合
        List<Sku> skus = goods.getSkuList();
        //循环将数据加入到数据库
        for (Sku sku : skus) {
            //构建SKU名称，采用SPU+规格值组装
            if((sku.getSpec()==null)){
                sku.setSpec("{}");
            }
            //获取Spu的名字
            String name = spu.getName();

            //将规格转换成Map
            Map<String,String> specMap = JSON.parseObject(sku.getSpec(), Map.class);
            //循环组装Sku的名字
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                name+="  "+entry.getValue();
            }
            sku.setName(name);
            //ID
            sku.setId(String.valueOf(idWorker.nextId()));
            //SpuId
            sku.setSpuId(spu.getId());
            //创建日期
            sku.setCreateTime(date);
            //修改日期
            sku.setUpdateTime(date);
            //商品分类ID
            sku.setCategoryId(spu.getCategory3Id());
            //分类名字
            sku.setCategoryName(category.getName());
            //品牌名字
            sku.setBrandName(brand.getName());
            //增加
            skuMapper.insertSelective(sku);

            //品牌分类关联
            CategoryBrand categoryBrand = new CategoryBrand();
            categoryBrand.setBrandId(spu.getBrandId());
            categoryBrand.setCategoryId(spu.getCategory3Id());
            int count = categoryBrandMapper.selectCount(categoryBrand);
            //如果没有查到此类关联，就创建关联
            if(count==0){
                categoryBrandMapper.insertSelective(categoryBrand);
            }
        }
    }

    /**
     * 根据spuId查询商品详情
     * @param spuId
     * @return
     */
    @Override
    public Goods findGoodsBySpuId(Long spuId) {
        //根据spuId查出第三级目录id
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //根据第三级目录id查出sku信息
        Sku sku = new Sku();
        sku.setCategoryId(spu.getCategory3Id());
        List<Sku> skuList = skuMapper.select(sku);
        //将sku,spu信息保存在goods里面
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    /**
     * 商品审核（审核通过自动上架）
     * @param spuId
     */
    @Override
    public void audit(Long spuId) {
        //根据spuId查出spu信息
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("该商品已经删除");
        }
        //通过审核
        spu.setStatus("1");
        //上架
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 下架
     * @param spuId
     */
    @Override
    public void pull(Long spuId) {
        //根据spuId查出spu信息
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("该商品已经删除");
        }
        //下架
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 批量下架
     * @param ids 需要下架的商品ID集合
     * @return
     */
    @Override
    public int pullMany(Long[] ids) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        criteria.andEqualTo("status","1");
        criteria.andEqualTo("isDelete","0");
        Spu spu = new Spu();
        spu.setIsMarketable("0");
        return spuMapper.updateByExampleSelective(spu,criteria);
    }

    /**
     * 上架
     * @param spuId
     */
    @Override
    public void put(Long spuId) {
        //根据spuId查出spu信息
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("该商品已经删除");
        }
        if(!spu.getStatus().equals("1")){
            throw new RuntimeException("未通过审核的商品不能上架！");
        }
        //上架
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 批量上架
     * @param ids 需要上架的商品ID集合
     * @return
     */
    @Override
    public int putMany(Long[] ids) {
        //update tb_spu isMarketable = "1" where id in(ids) and isDelete="0" and status=1
        Example example = new Example(Spu.class);
        //得到构建条件对象
        Example.Criteria criteria = example.createCriteria();
        //条件id
        criteria.andIn("id", Arrays.asList(ids));
        //状态条件：未删除
        criteria.andEqualTo("isDelete","0");
        //状态条件：已上架
        criteria.andEqualTo("status","1");
        //准备修改数据
        Spu spu = new Spu();
        spu.setIsMarketable("1");
        //第一个参数，修改的结果，第二个是修改的条件
        return spuMapper.updateByExampleSelective(spu,criteria);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andEqualTo("sn",searchMap.get("sn"));
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
           	}
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
           	}
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andEqualTo("isMarketable",searchMap.get("isMarketable"));
           	}
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
