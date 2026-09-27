package com.zwz.forum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

/**
 * 文件上传服务类
 */
@Service
public class UploadService {
    
    @Value("${service.download.address}")
    private String address;
    
    @Value("${service.ipAddr}")
    private String ipAddr;
    
    @Value("${server.port}")
    private String port;

    /**
     * 上传图片文件
     * @param file 图片文件
     * @return 图片访问URL
     */
    public String uploadImage(MultipartFile file) {
        return uploadFile(file, "image");
    }

    /**
     * 通用文件上传方法
     * @param file 上传的文件
     * @param type 文件类型
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String type) {
        OutputStream os = null;
        InputStream is = null;
        String newName = getNewName(file);
        try {
            is = file.getInputStream();
            byte[] bs = new byte[1024];
            int length;
            File tempFile = new File(address.concat(type));
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            os = new FileOutputStream(tempFile.getPath().concat(File.separator).concat(newName));
            while ((length = is.read(bs)) != -1) {
                os.write(bs, 0, length);
            }
            // 构建并返回文件访问URL
            return ipAddr.concat(":").concat(port).concat("/file/").concat(type).concat("/").concat(newName);
        } catch (Exception e) {
            System.out.println("文件上传异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 生成唯一的文件名
     * @param file 上传的文件
     * @return 新的文件名
     */
    public String getNewName(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String filename = file.getOriginalFilename();
        String newName = uuid + filename.substring(filename.lastIndexOf("."));
        return newName;
    }
}
