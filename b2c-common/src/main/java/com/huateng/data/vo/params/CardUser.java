package com.huateng.data.vo.params;

import lombok.Data;

@Data
public class CardUser {
    private String custId;

    private String status;

    private Object[] validCardCodes; //有效的活动编号
}
