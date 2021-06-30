package com.huateng.data.vo.json.req;

import lombok.Data;

/**
 * Created with b2c-service.
 * User: lxc
 * Date: 2019/12/05
 * Time: 16:44
 * Description:
 */
@Data
public class QueryCoupon {
    //会员号
    private String custId;
    //查询当前页
    private Integer pageNum;
    //查询页大小
    private Integer pageSize;
}
