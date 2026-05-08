package com.easy.pojo.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SignInVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 本月签到次数
     */
    private Integer signInCount;

    /**
     * 本月签到表
     */
    private List<Boolean> signInTable;

    /**
     * 本月剩余补签次数
     */
    private Integer signInRepairCount;

}
