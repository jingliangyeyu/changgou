package com.changgou.entity;

/**
 * @author zhouson
 * @create 2020-05-14 22:10
 */

import lombok.Data;

/**
 * 返回结果实体类
 */
@Data
public class Result {

    private boolean flag;//是否成功
    private Integer code;//返回码
    private String message;//返回消息
    private Object data;//返回数据

    public Result(boolean flag, Integer code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public Result() {
        this.flag = true;
        this.code = StatusCode.OK;
        this.message = "执行成功";
    }

    // getter and setter.....
}
