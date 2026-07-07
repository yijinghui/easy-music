package com.easy.result;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrollResult implements Serializable {
    private List list; // 结果集
    private Long lastId; // 用于数据库查询，衔接redis滚动分页
}
