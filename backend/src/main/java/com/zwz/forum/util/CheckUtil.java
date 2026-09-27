package com.zwz.forum.util;

/**
 * 文件类型检查工具类
 */
public class CheckUtil {

    /**
     * 检查是否为图片文件
     * @param filename 文件名
     * @return 是否为图片文件
     */
    public static Boolean isImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        String type = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return type.equals(".jpg") || type.equals(".jpeg") || type.equals(".png") || type.equals(".gif") || type.equals(".bmp");
    }
}
