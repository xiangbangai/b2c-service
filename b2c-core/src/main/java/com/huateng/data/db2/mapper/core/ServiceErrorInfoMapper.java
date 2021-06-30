package com.huateng.data.db2.mapper.core;

import com.huateng.data.model.db2.ServiceErrorInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("coreServiceErrorInfoMapper")
public interface ServiceErrorInfoMapper {

    List<ServiceErrorInfo> getAll();
}