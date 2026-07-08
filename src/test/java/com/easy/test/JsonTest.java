package com.easy.test;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easy.pojo.entity.Song;
import com.easy.result.Result;
import com.easy.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public class JsonTest {

    public static void main(String[] args) {
        Song song = new Song();
        song.setSongId(1L);
        System.out.println(JSONUtil.toJsonStr(song));
    }
}

