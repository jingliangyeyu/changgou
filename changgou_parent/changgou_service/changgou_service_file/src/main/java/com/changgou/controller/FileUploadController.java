package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhouson
 * @create 2020-05-17 23:19
 * 文件上传
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    @PostMapping
    public Result upload(@RequestParam(value="file") MultipartFile file) throws Exception{
        //封装文件信息
        String fileName = file.getOriginalFilename();
        FastDFSFile fastDFSFile = new FastDFSFile(
                //文件名字
                file.getOriginalFilename(),
                //文件字节数组
                file.getBytes(),
                //后缀名
                fileName.substring(fileName.lastIndexOf(".")+1)
        );
        //调用上传方法
        String[] uploads = FastDFSClient.upload(fastDFSFile);
        /**
         *         拼接访问地址,8080是nginx端口，用户访问图片是要先访问nginx,
         *         uploads[0]-组名，uploads[1]-获取文件存储路径
         */
        String url = "http://192.168.80.0:8080/"+uploads[0]+"/"+uploads[1];
        return new Result(true, StatusCode.OK,"上传成功",url);

    }
}
