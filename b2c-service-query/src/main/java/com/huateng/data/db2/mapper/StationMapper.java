package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.Station;

public interface StationMapper {

    Station selectByStationId(String stationId);

}