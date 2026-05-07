package com.easy.pojo.dto;

import com.easy.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserEmailLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户邮箱
     */
    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户邮箱", example = "2199791686@qq.com")
    private String email;


    /**
     * 验证码
     */
    @NotBlank(message = MessageConstant.CAPTCHA + MessageConstant.NOT_NULL)
    @Schema(description = "验证码", example = "123456")
    private String verificationCode;

}
