package com.huateng.data.vo.json.req;

import lombok.Data;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/11
 * Time: 11:09
 * Description:
 */
@Data
public class CouponUsable {

    private String custId; //会员id
    private Integer pageNum; //页码
    private Integer pageSize; //页大小
}
