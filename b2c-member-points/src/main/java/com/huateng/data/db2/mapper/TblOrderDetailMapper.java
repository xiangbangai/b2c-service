package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblOrderDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TblOrderDetailMapper {

    int updateByPrimaryKeySelective(TblOrderDetail tblOrderDetail);
}