package com.huateng.web.service;

import com.huateng.base.BaseService;
import com.huateng.data.db2.mapper.StationMapper;
import com.huateng.data.model.db2.Station;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class StationService extends BaseService {

    @Resource
    private StationMapper stationMapper;

    /**
     * 查询油站信息
     * @param stationId
     * @return
     */
    public Station getStationInfo(String stationId) {
        return this.stationMapper.selectByStationId(stationId);
    }
}
