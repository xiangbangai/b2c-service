package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.TblBonusPlanDetail;
import com.huateng.data.vo.params.BonusPlanDetailParams;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TblBonusPlanDetailMapper {

    List<TblBonusPlanDetail> queryBonusPlanDetail(BonusPlanDetailParams bonusPlanDetailParams);

    List<TblBonusPlanDetail> queryBonusPlanDetails(@Param("custId") String custId, @Param("bpPlanType") String bpPlanType, @Param("validates") List<String> validates);
}