package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblBonusDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TblBonusDetailMapper {

    List<TblBonusDetail> getByBonusSsn(String bonusSsn);

}