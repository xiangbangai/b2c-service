package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.TblBonusPlan;

public interface TblBonusPlanMapper {

    /**
     * 更新积分
     * @param bonusPlan
     * @return
     */
    void updatePlanBonus(TblBonusPlan bonusPlan);

    void insert(TblBonusPlan newBonusPlan);
}