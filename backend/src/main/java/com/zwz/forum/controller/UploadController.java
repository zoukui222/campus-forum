package com.zwz.forum.controller;

import com.zwz.forum.common.Result;
import com.zwz.forum.service.UploadService;
import com.zwz.forum.util.CheckUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 文件上传控制器
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * 上传图片文件
     * @param file 图片文件
     * @return 上传结果
     */
    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        
        if (CheckUtil.isImage(file.getOriginalFilename())) {
            String url = uploadService.uploadImage(file);
            if (url != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("url", url);
                return Result.success(data);
            } else {
                return Result.error(500, "文件上传失败");
            }
        }
        return Result.error(400, "文件格式错误，请上传图片文件");
    }
}
