package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.TblLabelGroup;
import com.huateng.data.vo.params.LabelGroupInfo;

import java.util.List;

public interface TblLabelGroupMapper {

    List<TblLabelGroup> queryLabelGroupByLabelGroupInfo(LabelGroupInfo info);
}