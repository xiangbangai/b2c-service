package com.huateng.web.service;

import com.github.pagehelper.PageHelper;
import com.huateng.base.BaseService;
import com.huateng.common.util.RestTemplateUtil;
import com.huateng.data.db3.mapper.EdclistnoMapper;
import com.huateng.data.db3.mapper.PayJMapper;
import com.huateng.data.db3.mapper.SaleJMapper;
import com.huateng.data.model.db3.Edclistno;
import com.huateng.data.model.db3.PayJ;
import com.huateng.data.model.db3.SaleJ;
import com.huateng.data.vo.params.InvoiceAmountOfMoney;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/17
 * Time: 12:08
 * Description: 发票库操作
 */
@Slf4j
@Service
public class InvoiceService extends BaseService {

    @Value("${WeChat.queryInvoice.url}")
    private String queryInvoceUrl;
    @Resource
    private EdclistnoMapper edclistnoMapper;
    @Resource
    private PayJMapper payJMapper;
    @Resource
    private SaleJMapper saleJMapper;

    /**
     * 查询EdcListNo总记录数
     * @param edclistno
     * @return
     */
    @Transactional(transactionManager = "dataSourceTransactionManagerDb3",isolation = Isolation.READ_UNCOMMITTED)
    public long queryEdcListNoCount(Edclistno edclistno) {
        return PageHelper.count(() -> this.edclistnoMapper.queryInfo(edclistno));
    }

    /**
     * 查询PayJ总记录数
     * @param payJ
     * @return
     */
    @Transactional(transactionManager = "dataSourceTransactionManagerDb3",isolation = Isolation.READ_UNCOMMITTED)
    public long queryPayJCount(PayJ payJ) {
        return PageHelper.count(() -> this.payJMapper.queryInfo(payJ));
    }

    /**
     * 查询SaleJ总记录数
     * @param saleJ
     * @return
     */
    @Transactional(transactionManager = "dataSourceTransactionManagerDb3",isolation = Isolation.READ_UNCOMMITTED)
    public long querySaleJCount(SaleJ saleJ) {
        return PageHelper.count(() -> this.saleJMapper.queryInfo(saleJ));
    }

    /**
     * 查询开票金额
     * @param saleJ
     */
    public String queryInvoiceAmountOfMoney(SaleJ saleJ) throws Exception {
        InvoiceAmountOfMoney invoiceAmountOfMoney = new InvoiceAmountOfMoney();
        invoiceAmountOfMoney.setListno(String.valueOf(saleJ.getListno()));
        invoiceAmountOfMoney.setPosId(saleJ.getPosId());
        invoiceAmountOfMoney.setShiftid(String.valueOf(saleJ.getShiftid()));
        invoiceAmountOfMoney.setStationId(saleJ.getShopid());
        invoiceAmountOfMoney.setWorkdate(saleJ.getWorkdate().substring(0,10));

        log.info("查询开票金额发送:{}", invoiceAmountOfMoney.toString());
        String result = RestTemplateUtil.httpPostUrl(queryInvoceUrl, PropertyUtils.describe(invoiceAmountOfMoney), null);
        log.info("查询开票金额返回：{}", result);
        return result;
    }

}
