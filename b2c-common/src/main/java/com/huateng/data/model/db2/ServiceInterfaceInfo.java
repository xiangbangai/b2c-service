package com.huateng.data.model.db2;

import lombok.Data;

import java.util.Date;

@Data
public class ServiceInterfaceInfo {
    private String id;
    private String url;
    private String serviceName;
    private Short isOpen;
    private Short serviceType;
    private Date createDateTime;
    private Date updateDateTime;
    private String operator;

}