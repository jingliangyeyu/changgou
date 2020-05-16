package com.changgou.goods.pojo;

import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "tb_brand")
@Data
public class Brand implements Serializable {
    @Id
    //品牌id
    private Integer id;
    //品牌名称
    private String name;
    //品牌图片地址
    private String image;
    //品牌的首字母
    private String letter;
    //排序
    private Integer seq;
}
