package com.changgou.entity;

/**
 * @author zhouson
 * @create 2020-05-14 22:16
 */

import lombok.Data;

import java.util.List;

/**
 * 分页结果类
 */
@Data
public class PageResult<T> {
    //总记录数
    private Long total;
    //记录
    private List<T> rows;

    public PageResult(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public PageResult() {
    }

    //getter and setter ......
}
