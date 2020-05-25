package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author zhouson
 * @create 2020-05-22 16:58
 * name = "goods" 需要访问的服务名（springApplicationName）
 */
@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {
    @GetMapping("/status/{status}")
    public Result<List<Sku>> findByStatus(@PathVariable String status);
}
