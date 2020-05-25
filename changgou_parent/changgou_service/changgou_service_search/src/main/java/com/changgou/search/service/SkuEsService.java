package com.changgou.search.service;

import java.util.Map;

/**
 * @author zhouson
 * @create 2020-05-22 17:07
 */
public interface SkuEsService {
    /***
     * 导入SKU数据
     */
    void importSku();

    /***
     * 搜索    这里采用map是因为前台传过来的搜索对象-关键词可能不只一个
     * 返回数据是map因为里面有查询出来的数据，也有其他数据(如分页数)
     * @param searchMap
     * @return
     */
    Map search(Map<String, String> searchMap);
}
