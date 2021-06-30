package com.huateng.web.service;

import com.huateng.base.BaseService;
import com.huateng.data.db1.mapper.TblCardActivityMapper;
import com.huateng.data.model.db1.CardActivityAndRule;
import com.huateng.data.vo.params.CardActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class QueryCardActivityService extends BaseService {

    @Resource
    private TblCardActivityMapper tblCardActivityMapper;

    /**
     * 根据活动编号集合查询活动与规则信息
     * @param cardActivity
     * @return
     */
    public List<CardActivityAndRule> queryCardActivityAndRuleList(CardActivity cardActivity) {
        return this.tblCardActivityMapper.queryCardActivityAndRuleList(cardActivity);
    }
}
