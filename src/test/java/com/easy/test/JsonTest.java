package com.easy.test;


import cn.hutool.json.JSONUtil;
import com.easy.pojo.entity.Song;

public class JsonTest {

    public static void main(String[] args) {
        Song song = new Song();
        song.setSongId(1L);
        System.out.println(JSONUtil.toJsonStr(song));
    }
}
