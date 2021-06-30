package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db1.CardActivityAndRule;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.CardActivity;
import com.huateng.web.service.QueryCardActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Api(tags = "活动相关查询服务")
@RestController
public class CardActivityController extends BaseController {


    @Resource
    private QueryCardActivityService queryCardActivityService;

    @ApiOperation(value = "查询活动与规则", notes = "如果有活动则返回活动与规则列表")
    @ApiImplicitParam(name = "cardActivity", value = "活动参数", required = true, dataType = "CardActivity")
    @PostMapping("/activity/cardActivityAndRuleList")
    public ResInfo cardActivityAndRuleList(@RequestBody CardActivity cardActivity) throws Exception {
        ResInfo resInfo = new ResInfo();
        List<CardActivityAndRule> list = this.queryCardActivityService.queryCardActivityAndRuleList(cardActivity);

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        if(!list.isEmpty()) {
            resInfo.setData(JacksonUtil.toJson(list));
        }
        return resInfo;
    }
}
