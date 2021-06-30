package com.huateng.data.vo.params;

import lombok.Data;

import java.util.Date;

@Data
public class MemberCancellation {
    private String custId; //会员id
    private Date txnDate;//交易时间
}
