package com.easy.test;


import com.minio.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@SpringBootTest
public class MinioStarterTest {

    @Autowired
    private MinioTemplate minioTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void testUploadFile() {

        // 上传图片
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes(StandardCharsets.UTF_8));

        String url = minioTemplate.uploadFile(file, "test");
        System.out.println(url);
    }

    @Test
    public void testDownloadFile() throws Exception {
        String url=minioTemplate.generatePresignedUrl("songs/f9580a5c-39a6-4950-8bc0-18d8a8e3612c-Aaron Carter - Fool's Gold.mp3");
        System.out.println(url);
    }
}
