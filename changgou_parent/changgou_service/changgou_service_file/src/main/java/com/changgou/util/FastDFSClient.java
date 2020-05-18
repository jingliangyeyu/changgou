package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;

/**
 * @author zhouson
 * @create 2020-05-17 22:47
 * 文件操作
 */
public class FastDFSClient {
    /**
     * 初始化加载TrackerServer配置
     */
    static {
        try {
            //获取classpath下文件的路径
            String path = new ClassPathResource("fdfs_client.conf").getPath();
            //加载Tracker链接信息
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] upload(FastDFSFile file) throws Exception{
        //NameValuePair是上传的附加数据，可以添加,至少要有值，下面的方法NameValuePair[]参数不能为空，否则空指针异常
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author",file.getAuthor());
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer connection = trackerClient.getConnection();
        //通过TrackerServer的连接信息可以获取Storage的连接信息，创建TrackerClient对象存储Storage的连接信息
        StorageClient storageClient = new StorageClient(connection, null);
        String[] uploadResults = storageClient.upload_file(file.getContent(), file.getExt(), meta_list);
        return uploadResults;
    }
}
