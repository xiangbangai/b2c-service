package com.huateng.data.db2.mapper.core;

import com.huateng.data.model.db2.ServiceChannel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("coreServiceChannelMapper")
public interface ServiceChannelMapper {

    List<ServiceChannel> getChannel();
}