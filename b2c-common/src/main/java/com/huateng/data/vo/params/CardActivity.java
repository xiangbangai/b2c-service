package com.huateng.data.vo.params;

import lombok.Data;

@Data
public class CardActivity {
    private String cardStatus; //活动状态
    private String validDate; //活动有效期>=validDate 此日期之前
}
