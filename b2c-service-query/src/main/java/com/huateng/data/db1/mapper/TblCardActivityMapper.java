package com.huateng.data.db1.mapper;


import com.huateng.data.model.db1.CardActivityAndRule;
import com.huateng.data.vo.params.CardActivity;

import java.util.List;

public interface TblCardActivityMapper {

    List<CardActivityAndRule> queryCardActivityAndRuleList(CardActivity cardActivity);
}