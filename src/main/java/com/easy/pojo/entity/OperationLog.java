package com.easy.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_log")
public class OperationLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime operatorTime;

    /**
     * 操作的类名
     */
    private String className;

    /**
     * 操作的方法名
     */
    private String methodName;

    /**
     * 方法参数
     */
    private String methodParams;

    /**
     * 返回值
     */
    private String returnValue;

    /**
     * 方法运行耗时（ms）
     */
    private Long costTime;
}
