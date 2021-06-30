package com.huateng.data.model.db2;

import lombok.Data;

import java.util.Date;

@Data
public class ServiceNotProduceMidtype {
    private String id;

    private Integer middleType;

    private String middleTypeName;

    private String creator;

    private Date createTime;

    private String modifier;

    private Date modifyTime;

}