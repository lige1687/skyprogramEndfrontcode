package com.sky.controller.admin;

import com.fasterxml.jackson.core.io.IOContext;
import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
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

@RestController
@RequestMapping("/admin/common")
@Api (tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @ApiOperation("upload 图片代码")
    // 接受 前端发来的file 文件 , 直接在形参中接受即可, 必须同名才不需要写注解value 进行匹配参数
    @PostMapping("/upload")

    public Result<String> upload (MultipartFile file){
        log.info("文件上传操作{}" , file);
        // 要什么参数类型, 直接转就完了. 这里需要两个参数 , 要清楚参数的含义
        // 第一个参数就是你的file, 第二个参数是 该图片在oss的名字
        // 为了 避免oss 在 相同名字的文件的时候 会覆盖 , 这里使用重命名
        try {
            // 这里存储在 oss需要有后缀, 所以这里动态的获取后缀名先
            String originalFilename = file.getOriginalFilename();

            String substring = originalFilename.substring(originalFilename.lastIndexOf("."));// 从最后一个点开始截取
            String newObjectname = UUID.randomUUID().toString() + substring;
// 获得新的请求路径
            String path = aliOssUtil.upload(file.getBytes(), newObjectname);
             return Result.success(path);
        } catch (IOException e) {
            log.info("文件上传失败{}", e);
        }
// 返回常量 , 较为规范
        return  Result.error(MessageConstant.UPLOAD_FAILED) ;
    }
}
