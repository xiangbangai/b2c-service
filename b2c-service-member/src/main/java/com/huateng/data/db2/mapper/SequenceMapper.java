package com.huateng.data.db2.mapper;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface SequenceMapper {
    BigDecimal getSeq(@Param("seqName") String seqName);
}
