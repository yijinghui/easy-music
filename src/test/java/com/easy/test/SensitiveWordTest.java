package com.easy.test;


import com.easy.utils.SensitiveWordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
public class SensitiveWordTest {


    @Autowired
    private SensitiveWordUtil sensitiveWordUtil;

    @Test
    void test01(){
        String s="f uck you";
        String filter = sensitiveWordUtil.filter(s);
        System.out.println(filter);
    }
}
