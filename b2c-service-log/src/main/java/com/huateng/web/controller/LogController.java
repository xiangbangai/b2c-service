package com.huateng.web.controller;

import com.dianping.cat.Cat;
import com.huateng.base.BaseController;
import com.huateng.data.vo.params.LogInfo;
import com.huateng.web.service.SendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/7/31
 * Time: 16:47
 * Description: 日志服务入口
 */
@Slf4j
@Api(tags = "日志服务")
@RestController
public class LogController extends BaseController {

    @Resource
    private SendService sendService;


    /**
     * 发送远端日志服务
     * @param logInfo
     * @return
     */
    @ApiOperation(value = "发送日志信息", notes = "上送远端日志服务")
    @ApiImplicitParam(name = "LogInfo", value = "日志参数信息", required = true, dataType = "LogInfo")
    @PostMapping("/service/sendRemoteService")
    public void sendRemoteService(@RequestBody LogInfo logInfo) {
        CompletableFuture.runAsync(() -> this.sendService.sendLogSystem(logInfo)).exceptionally(e -> {
            Cat.logError("日志上送失败...",e);
            return null;
        });
    }
}
