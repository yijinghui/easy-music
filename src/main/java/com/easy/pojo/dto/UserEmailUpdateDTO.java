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
public class UserEmailUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户邮箱
     */
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @Schema(description = "旧邮箱",  example = "480395924@qq.com")
    private String oldEmail;

    /**
     * 验证码
     * 验证码格式：6位字符（大小写字母、数字）
     */
    @NotBlank(message = MessageConstant.VERIFICATION_CODE + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[0-9a-zA-Z]{6}$", message = MessageConstant.VERIFICATION_CODE + MessageConstant.FORMAT_ERROR)
    @Schema(description = "验证码", example = "123456")
    private String oldVerificationCode;



    /**
     * 用户邮箱
     */
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户邮箱", example = "480395924@qq.com")
    private String newEmail;

    /**
     * 验证码
     * 验证码格式：6位字符（大小写字母、数字）
     */
    @NotBlank(message = MessageConstant.VERIFICATION_CODE + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[0-9a-zA-Z]{6}$", message = MessageConstant.VERIFICATION_CODE + MessageConstant.FORMAT_ERROR)
    @Schema(description = "验证码", example = "123456")
    private String newVerificationCode;
}
