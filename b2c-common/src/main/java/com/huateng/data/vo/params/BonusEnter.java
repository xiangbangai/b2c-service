package com.huateng.data.vo.params;

import com.huateng.data.model.db2.ServiceOrder;
import com.huateng.data.model.db2.TblCustInf;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class BonusEnter {
    private ServiceOrder order;

    private TblCustInf custInf;//会员信息

    private List<BonusAccItf> bonus;//积分计算结果

    private List<Goods> goods; //商品
}
