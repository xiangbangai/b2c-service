package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.Station;
import com.huateng.data.vo.ResInfo;
import com.huateng.web.service.StationService;
import com.huateng.web.service.ValidateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@Api(tags = "油站相关查询")
@RestController
public class StationController extends BaseController {

    @Resource
    private ValidateService validateService;
    @Resource
    private StationService stationService;
    /**
     * 查询订单信息
     * @param stationId
     * @return
     */
    @ApiOperation(value = "查询油站信息", notes = "返回油站对象")
    @ApiImplicitParam(name = "stationId", value = "油站编号", required = true, dataType = "String")
    @PostMapping("/station/info")
    public ResInfo queryStationInfo(String stationId) throws Exception {
        ResInfo resInfo = new ResInfo();
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        resInfo.setData(JacksonUtil.toJson(this.stationService.getStationInfo(stationId)));
        return resInfo;
    }
}
