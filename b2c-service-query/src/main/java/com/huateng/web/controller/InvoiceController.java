package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db3.Edclistno;
import com.huateng.data.model.db3.PayJ;
import com.huateng.data.model.db3.SaleJ;
import com.huateng.data.vo.ResInfo;
import com.huateng.web.service.InvoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/17
 * Time: 11:56
 * Description: 电子发票库相关查询
 */
@Slf4j
@Api(tags = "电子发票库相关查询")
@RestController
public class InvoiceController extends BaseController {

    @Resource
    private InvoiceService invoiceService;

    @ApiOperation(value = "查询EdcListNo总数（脏读）", notes = "返回EdcListNo总数")
    @ApiImplicitParam(name = "edclistno", value = "edclistno", required = true, dataType = "Edclistno")
    @PostMapping("/invoice/queryEdcListNoCount")
    ResInfo queryEdcListNoCount (@RequestBody Edclistno edclistno) {
        ResInfo resInfo = new ResInfo();
        resInfo.setData(String.valueOf(this.invoiceService.queryEdcListNoCount(edclistno)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询PayJ总数（脏读）", notes = "返回PayJ总数")
    @ApiImplicitParam(name = "payJ", value = "payJ", required = true, dataType = "PayJ")
    @PostMapping("/invoice/queryPayJCount")
    ResInfo queryPayJCount (@RequestBody PayJ payJ) {
        ResInfo resInfo = new ResInfo();
        resInfo.setData(String.valueOf(this.invoiceService.queryPayJCount(payJ)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询SalJ总数（脏读）", notes = "返回SalJ总数")
    @ApiImplicitParam(name = "saleJ", value = "saleJ", required = true, dataType = "SaleJ")
    @PostMapping("/invoice/querySaleJCount")
    ResInfo querySaleJCount (@RequestBody SaleJ saleJ) {
        ResInfo resInfo = new ResInfo();
        resInfo.setData(String.valueOf(this.invoiceService.querySaleJCount(saleJ)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询开票金额", notes = "官微返回结果")
    @ApiImplicitParam(name = "saleJ", value = "saleJ", required = true, dataType = "SaleJ")
    @PostMapping("/invoice/queryInvoiceAmountOfMoney")
    ResInfo queryInvoiceAmountOfMoney (@RequestBody SaleJ saleJ) throws Exception {
        ResInfo resInfo = new ResInfo();
        resInfo.setData(this.invoiceService.queryInvoiceAmountOfMoney(saleJ));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }


}
