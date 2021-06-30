package com.huateng.data.vo.params;

import lombok.Data;

@Data
public class TblCustLabelParams {
    private String custId;
    private String labelType;
    private String status;
    private String dateBetween;
}