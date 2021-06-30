package com.huateng.data.db2.mapper;



import com.huateng.data.model.db2.TblCustLabel;
import com.huateng.data.vo.params.TblCustLabelParams;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TblCustLabelMapper {

    List<String> queryCustLabel(@Param("custId") String custId,@Param("expDate") String expDate);

    int insertSelective(TblCustLabel record);

    int updateByPrimaryKeySelective(TblCustLabel record);

    List<TblCustLabel> getInfo(TblCustLabelParams tblCustLabelParams);
}