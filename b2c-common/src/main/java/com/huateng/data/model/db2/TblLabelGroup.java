package com.huateng.data.model.db2;

import lombok.Data;

import java.util.Date;

@Data
public class TblLabelGroup {
    private String id;

    private String groupName;

    private String msgContent;

    private String advContent;

    private String advUrl;

    private String creator;

    private Date createTime;

    private String modifier;

    private Date modifyTime;

}