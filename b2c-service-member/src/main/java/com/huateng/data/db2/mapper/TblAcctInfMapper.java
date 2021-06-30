package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.TblAcctInf;

public interface TblAcctInfMapper {

    int insertSelective(TblAcctInf record);

    int updateByPrimaryKeySelective(TblAcctInf record);

}