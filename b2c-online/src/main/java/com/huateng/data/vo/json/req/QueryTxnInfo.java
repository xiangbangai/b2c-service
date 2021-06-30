package com.huateng.data.vo.json.req;

import lombok.Data;

import java.util.Date;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/15
 * Time: 21:20
 * Description:
 */
@Data
public class QueryTxnInfo {

    private String acctId;
    private String beginDate;
    private String endDate;
    private Integer pageNum;
    private Integer PageSize;

}
