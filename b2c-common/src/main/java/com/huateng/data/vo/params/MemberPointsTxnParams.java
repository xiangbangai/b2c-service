package com.huateng.data.vo.params;

import lombok.Data;

import java.util.Date;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/15
 * Time: 22:32
 * Description:
 */
@Data
public class MemberPointsTxnParams {

    private String custId;
    private Date beginDate;
    private Date endDate;
    private Integer pageNum;
    private Integer PageSize;
}
