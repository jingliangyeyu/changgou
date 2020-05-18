package com.changgou.goods.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "tb_brand")
@Data
public class Brand implements Serializable {
    //品牌id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;
    //品牌名称
    @Column(name="name")
    private String name;
    //品牌图片地址
    @Column(name="image")
    private String image;
    //品牌的首字母
    @Column(name="letter")
    private String letter;
    //排序
    @Column(name="seq")
    private Integer seq;
}
