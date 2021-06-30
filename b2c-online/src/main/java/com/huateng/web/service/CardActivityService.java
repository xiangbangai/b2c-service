package com.huateng.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huateng.base.BaseService;
import com.huateng.common.util.JacksonUtil;
import com.huateng.data.model.db1.CardActivityAndRule;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.CardActivity;
import com.huateng.service.remote.QueryActivityRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class CardActivityService extends BaseService {

    @Resource
    private QueryActivityRemote queryActivityRemote;

    /**
     * 根据活动编号集合查询活动和规则信息
     * @param cardActivity
     * @return
     */
    public List<CardActivityAndRule> queryCardActivityAndRuleList(CardActivity cardActivity) throws Exception {
        ResInfo resInfo = this.queryActivityRemote.cardActivityAndRuleList(cardActivity);
        return JacksonUtil.toObject(getResJson(resInfo),new TypeReference<List<CardActivityAndRule>>(){});
    }
}
