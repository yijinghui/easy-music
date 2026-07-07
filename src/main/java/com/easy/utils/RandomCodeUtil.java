package com.easy.utils;

import java.util.Random;

public class RandomCodeUtil {

    private static final String DIGITS = "0123456789";
    private static final int LENGTH = 6; // 生成的字符串长度
    private static final Random random = new Random();

    /**
     * 生成随机的6位数字验证码
     *
     * @return 6位数字字符串
     */
    public static String generateRandomCode() {
        StringBuilder stringBuilder = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(DIGITS.length());
            stringBuilder.append(DIGITS.charAt(index));
        }
        return stringBuilder.toString();
    }

}
