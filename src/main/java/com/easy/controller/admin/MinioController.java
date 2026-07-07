package com.easy.controller.admin;


import com.easy.result.Result;
import com.minio.MinioTemplate;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Tag(name = "文件相关接口")
@RequiredArgsConstructor
public class MinioController {

    private final MinioTemplate minioTemplate;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, String folder) {
        return Result.success("上传成功", minioTemplate.uploadFile(file, folder));
    }

    @DeleteMapping("/delete")
    public Result delete(String fileName) {
        minioTemplate.deleteFile(fileName);
        return Result.success();
    }


}
