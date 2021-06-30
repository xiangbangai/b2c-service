package com.huateng.data.vo.json.res;

import lombok.Data;

import java.util.List;

@Data
public class ResCouponUsable {
    private int pageNum; //当前页
    private int pageSize; //页面大小
    private int pages; //总页数
    private long total; //总记录数
    private List<CouponInfo> list;
}
