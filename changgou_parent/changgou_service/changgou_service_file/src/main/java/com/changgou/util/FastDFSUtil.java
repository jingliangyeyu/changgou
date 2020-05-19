package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhouson
 * @create 2020-05-17 22:47
 * 文件操作
 */
public class FastDFSUtil {
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

    /**
     * 封装创建StorageClient的方法
     * @return StorageClient
     * @throws Exception
     */
    public static StorageClient getStorageClient() throws Exception{
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerServer的连接信息可以获取Storage的连接信息，创建TrackerClient对象存储Storage的连接信息
        return new StorageClient(trackerServer,null);
    }
    /***
     * 封装创建TrackerServer的方法
     * @return
     * @throws IOException
     */
    private static TrackerServer getTrackerServer() throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return  trackerServer;
    }

    /**
     * 获取Storage组
     * @return
     */
    public static StorageServer[] getStorageInfo(String groupName){
        try {
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = getTrackerServer();
            return trackerClient.getStoreStorages(trackerServer,groupName);
        } catch (IOException e) {
            e.printStackTrace();
        }
            return null;
    }
    /***
     * 获取指定Storage组信息,IP和端口
     * @param groupName
     * @param remoteFileName
     * @return ServerInfo
     * @throws IOException
     */
    public static ServerInfo[] getFetchStorages(String groupName, String remoteFileName) throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }
    /***
     * 获取Tracker服务地址
     * @return
     * @throws IOException
     */
    public static String getTrackerUrl() throws IOException {
        return "http://"+getTrackerServer().getInetSocketAddress().getHostString()+":"+ClientGlobal.getG_tracker_http_port()+"/";
    }

    /**
     * 文件上传
     * @param file
     * @return
     * @throws Exception
     */
    public static String[] upload(FastDFSFile file) throws Exception{
        //NameValuePair是上传的附加数据，可以添加,至少要有值，下面的方法NameValuePair[]参数不能为空，否则空指针异常
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author",file.getAuthor());
        StorageClient storageClient = getStorageClient();
        String[] uploadResults = storageClient.upload_file(file.getContent(), file.getExt(), meta_list);
        return uploadResults;
    }

    /**
     * 获取文件信息，供下载使用
     * @param groupName
     * @param remoteFileName 文件存储路径名字（组后面的路径） M00/00/00/wKhQAF7CAAKASKxgAAI-6XnibF0926.png
     * @return FileInfo
     */
    public static FileInfo getFileInfo(String groupName,String remoteFileName){
        FileInfo fileInfo =null;
        try {
            StorageClient storageClient = getStorageClient();
            fileInfo = storageClient.get_file_info(groupName,remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileInfo;
    }

    /**
     * 下载文件
     * @param groupName
     * @param remoteFileName
     * @return
     */
    public static InputStream downFile(String groupName,String remoteFileName){
        try {
            StorageClient storageClient = getStorageClient();
            byte[] downloadFile = storageClient.download_file(groupName, remoteFileName);
            InputStream ins = new ByteArrayInputStream(downloadFile);
            return ins;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void deleteFile(String groupName,String remoteFileName){
        try {
            StorageClient storageClient = getStorageClient();
            storageClient.delete_file(groupName,remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
