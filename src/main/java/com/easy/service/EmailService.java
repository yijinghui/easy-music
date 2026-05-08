package com.easy.service;


public interface EmailService {

    // 发送邮件
    boolean sendEmail(String to, String subject, String content);

    // 发送验证码邮件
    String sendVerificationCodeEmail(String email);
}
