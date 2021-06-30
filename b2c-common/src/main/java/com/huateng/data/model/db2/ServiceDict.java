package com.huateng.data.model.db2;

import lombok.Data;

@Data
public class ServiceDict {
    private String id;

    private String parentId;

    private Integer dictLevel;

    private String dictKey;

    private String dictValue;

    private String dictDesc;

}