package com.huateng.data.db2.mapper.core;


import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("coreTblSpecialRuleMapper")
public interface TblSpecialRuleMapper {

    List<String> getCardBin();
}