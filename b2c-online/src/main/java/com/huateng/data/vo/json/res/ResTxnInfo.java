package com.huateng.data.vo.json.res;

import com.huateng.data.model.db2.ServiceOrderTxnInfo;
import lombok.Data;

import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/22
 * Time: 16:54
 * Description:
 */
@Data
public class ResTxnInfo {
    private int pageNum; //当前页
    private int pageSize; //页面大小
    private int pages; //总页数
    private long total; //总记录数
    private List<ServiceOrderTxnInfo> list;
}
