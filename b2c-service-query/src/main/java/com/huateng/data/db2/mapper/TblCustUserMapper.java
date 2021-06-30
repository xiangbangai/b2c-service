package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.TblCustUser;

import java.util.List;

public interface TblCustUserMapper {

    TblCustUser queryCustUser(String custId);

    List<TblCustUser> queryTblCustUsersByCustId(String custId);
}