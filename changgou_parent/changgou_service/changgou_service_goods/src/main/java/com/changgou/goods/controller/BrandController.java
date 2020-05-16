package com.changgou.goods.controller;



import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhouson
 * @create 2020-05-14 23:54
 */
@RestController
@CrossOrigin
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 查询所有品牌
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Brand> brands = brandService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",brands);
    }
    /**
     * 根据id查询品牌
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable("id") Integer id){
        Brand brand = brandService.findById(id);
        return new Result(true, StatusCode.OK,"查询成功",brand);
    }
    /**
     * 添加品牌
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Brand brand){
        brandService.add(brand);
        return new Result(true,StatusCode.OK,"添加成功");
    }
    /**
     * 更新品牌
     * @return
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id,@RequestBody Brand brand){
        brand.setId(id);
        brandService.update(brand);
        return new Result(true,StatusCode.OK,"更新成功");
    }
    /**
     * 删除品牌
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
       brandService.delete(id);
       return new Result(true,StatusCode.OK,"删除成功");
    }
}
