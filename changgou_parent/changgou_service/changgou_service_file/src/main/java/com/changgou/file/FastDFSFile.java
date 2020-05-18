package com.changgou.file;

import lombok.Data;

/**
 * @author zhouson
 * @create 2020-05-17 22:45
 * 上传文件封装类
 */
@Data
public class FastDFSFile {
    private String name;
    private byte[] content;
    private String ext;
    private String md5;
    private String author;

    public FastDFSFile(String name, byte[] content, String ext) {
        this.name = name;
        this.content = content;
        this.ext = ext;
    }
}
