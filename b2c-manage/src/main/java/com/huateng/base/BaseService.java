package com.huateng.base;

import com.huateng.common.util.SnowflakeIdWorker;

import javax.annotation.Resource;


public abstract class BaseService {

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    protected long getTxnId() {
        return this.snowflakeIdWorker.nextId();
    }
}
