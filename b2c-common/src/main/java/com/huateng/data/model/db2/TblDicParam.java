package com.huateng.data.model.db2;

import lombok.Data;

@Data
public class TblDicParam {
    private Integer pkDicParam;

    private String paramCode;

    private String paramName;

    private String paramDesc;

    private Integer paramOrder;

    private String parentCode;

    private String field1;

    private String crtTime;

    private String updTime;

    private Integer objVersion;

    private String flagEnable;

    private String usageKey;

    private String paramDesc2;

    private String paramDesc3;

    private String paramSystem;

}