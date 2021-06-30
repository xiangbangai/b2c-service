package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblBonusDetail;

public interface TblBonusDetailMapper {

    int insert(TblBonusDetail tblBonusDetail);

    int updateByPrimaryKeySelective(TblBonusDetail oraBonusDetail);

    int updateReturnFlagByPrimaryKey(TblBonusDetail oraBonusDetail);
}