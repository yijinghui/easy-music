package com.easy.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserPageQueryDTO extends PageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;


    /**
     * 用户名
     */
    private String username;


    /**
     * 用户状态：0-启用，1-禁用
     */
    private Integer userStatus;
}
