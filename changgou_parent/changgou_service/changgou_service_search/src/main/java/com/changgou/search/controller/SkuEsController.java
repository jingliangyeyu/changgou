package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.SkuEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author zhouson
 * @create 2020-05-22 17:49
 */
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SkuEsController {
    @Autowired
    private SkuEsService skuEsService;

    /**
     * 导入数据
     * @return
     */
    @GetMapping("/import")
    public Result importSku(){
        skuEsService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库中成功！");
    }
    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String,String> searchMap){
        return  skuEsService.search(searchMap);
    }
}
