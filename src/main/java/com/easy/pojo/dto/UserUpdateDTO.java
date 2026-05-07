package com.easy.pojo.dto;

import com.easy.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名
     * 用户名格式：4-16位字符（字母、数字、下划线、连字符）
     */
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    private String username;

    /**
     * 用户手机号
     * 手机号格式：1开头，11位数字
     */
    @Pattern(regexp = "^1[3456789]\\d{9}$", message = MessageConstant.PHONE + MessageConstant.FORMAT_ERROR)
    private String phone;

    /**
     * 用户简介
     * 用户简介格式：100 字以内
     */
    @Pattern(regexp = "^.{0,100}$", message = MessageConstant.WORD_LIMIT_ERROR)
    private String introduction;





}
