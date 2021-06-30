package com.huateng.data.model.db2;

import lombok.Data;

import java.util.Date;

@Data
public class Station {
    private String stationId;

    private String name;

    private String shortName;

    private Integer stationTypeId;

    private Integer status;

    private String manager;

    private String address;

    private String telNo;

    private Integer marketRegionId;

    private Integer priceRegionId;

    private Integer workRegionId;

    private Integer financeRegionId;

    private Integer id;

    private Date lastUpdate;

    private String lastUpdater;

    private Integer version;

    private String clazz;

    private String filialeId;

}