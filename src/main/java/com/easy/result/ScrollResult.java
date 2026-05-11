package com.easy.result;


import lombok.Data;

import java.util.List;

@Data
public class ScrollResult<T> {
    private List<T> list; // 结果集
    private Integer offset; // 下一页偏移量（针对与最小时间戳时间戳相同的情况，跳过以防止ZSORTEDSET重复查询）
    private String minTime; // 下一页最小时间戳
}
