package com.huateng.data.db2.mapper.core;

import com.huateng.data.model.db2.ServiceDict;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("coreServiceDictMapper")
public interface ServiceDictMapper {

    List<ServiceDict> getDict();
}