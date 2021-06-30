package com.huateng.web.service;

import com.huateng.base.BaseService;
import com.huateng.common.util.JacksonUtil;
import com.huateng.data.model.db2.TblCustInf;
import com.huateng.data.vo.ResInfo;
import com.huateng.service.remote.QueryCustRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class CustService extends BaseService {

    @Resource
    private QueryCustRemote queryCustRemote;

    /**
     * 查询客户信息
     * @param id
     * @return
     */
    public TblCustInf queryCustInfo(String id) throws Exception {
        ResInfo resInfo = this.queryCustRemote.queryCustInfo(id);
        return JacksonUtil.toObject(getResJson(resInfo), TblCustInf.class);
    }
}
