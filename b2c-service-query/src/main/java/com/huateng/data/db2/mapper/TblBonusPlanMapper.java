package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblBonusPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TblBonusPlanMapper {

    TblBonusPlan queryCustIntegral(@Param("custId") String custId,
                                         @Param("integralType") String integralType,
                                         @Param("date") String date);

    TblBonusPlan queryBonusPlanByCustId(@Param("custId") String custId,@Param("bpPlanType") String bpPlanType);
}