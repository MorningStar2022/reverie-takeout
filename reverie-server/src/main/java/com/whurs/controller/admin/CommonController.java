package com.whurs.controller.admin;

import com.whurs.constant.MessageConstant;
import com.whurs.result.Result;
import com.whurs.utils.AliOssUtil;
import com.whurs.utils.MinioUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {
    /**
     * 文件上传
     * @param file
     * @return
     */
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private MinioUtil minioUtil;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);
        try {
            // 避免文件覆盖
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
            String upload = minioUtil.upload(file, fileName);
            return Result.success(upload);
        } catch (Exception e) {
            log.error("文件上传失败:{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
