package com.huateng.data.model.db2;

import lombok.Data;

@Data
public class ServiceChannel {
    private String id;

    private String parentId;

    private Integer mustProxy;

    private String channelName;

    private String certInfo;

    private Integer channelType;

}