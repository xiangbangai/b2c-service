package com.huateng.data.vo.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendInvoice {
    @JsonProperty("openid")
    private String openId;
    @JsonProperty("stationId")
    private String shopid;
    @JsonProperty("posid")
    private String posId;
    @JsonProperty("listno")
    private Integer listno;
    @JsonProperty("workdate")
    private String workdate;
    @JsonProperty("shiftid")
    private Integer shiftid;
}