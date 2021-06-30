package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.SnowflakeIdWorker;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/9/3
 * Time: 12:21
 * Description:
 */
@RestController
public class TestController extends BaseController {

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @RequestMapping("/test")
    public long test() {
        return snowflakeIdWorker.nextId();
    }
}
