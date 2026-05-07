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
public class UserRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     * 用户名格式：4-16位字符（字母、数字、下划线、连字符）
     */
    @Schema(description = "用户名", example = "user_01")
    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    private String username;

    /**
     * 用户密码
     * 密码格式：8-18 位数字、字母、符号的任意两种组合
     */
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    @Schema(description = "用户密码", example = "12345678")
    // @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z\\W]{8,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Pattern(regexp = "^[A-Za-z0-9]{8,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    private String password;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱", example = "2199791686@qq.com")
    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    private String email;

    /**
     * 验证码
     * 验证码格式：6位字符（大小写字母、数字）
     */
    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = MessageConstant.VERIFICATION_CODE + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[0-9a-zA-Z]{6}$", message = MessageConstant.VERIFICATION_CODE + MessageConstant.FORMAT_ERROR)
    private String verificationCode;

}
