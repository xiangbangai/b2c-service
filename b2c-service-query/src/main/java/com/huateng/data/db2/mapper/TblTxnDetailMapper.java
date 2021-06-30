package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblTxnDetail;

public interface TblTxnDetailMapper {

    TblTxnDetail getByAcqSsn(String acqSsn);
}