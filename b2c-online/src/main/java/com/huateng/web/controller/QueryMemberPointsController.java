package com.huateng.web.controller;

import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceOrderTxnInfo;
import com.huateng.data.model.db2.TblCustInf;
import com.huateng.data.vo.json.Request;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.json.req.QueryTxnInfo;
import com.huateng.data.vo.json.res.ResTxnInfo;
import com.huateng.data.vo.params.MemberPointsTxnParams;
import com.huateng.web.service.CustService;
import com.huateng.web.service.MemberPointsService;
import com.huateng.web.service.ValidateService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/15
 * Time: 21:17
 * Description: 查询会员积分相关
 */
@RestController
public class QueryMemberPointsController extends BaseController {

    @Resource
    private ValidateService validateService;
    @Resource
    private CustService custService;
    @Resource
    private MemberPointsService memberPointsService;

    /**
     * 查询交易流水
     * @return
     */
    @PostMapping("/query/memberPoints/txnInfoPageList")
    public Response queryTxnInfo() throws Exception {
        Response response = new Response();

        Request request;
        QueryTxnInfo queryTxnInfo;
        request = getObject(QueryTxnInfo.class);
        queryTxnInfo = (QueryTxnInfo) request.getData();

        /**校验数据**/
        this.validateService.validateForString(queryTxnInfo.getAcctId(), SysConstant.E01000003); //acctId不可为空
        this.validateService.validateForObject(queryTxnInfo.getPageNum(), SysConstant.E01000011); //pageNum不可为空
        this.validateService.validateForObject(queryTxnInfo.getPageSize(), SysConstant.E01000004); //pageSize不可为空
        this.validateService.validateForDate(queryTxnInfo.getBeginDate(),DateUtil.DATE_FORMAT_SHORT, SysConstant.E01000051); //beginDate格式错误
        this.validateService.validateForDate(queryTxnInfo.getEndDate(),DateUtil.DATE_FORMAT_SHORT, SysConstant.E01000052); //endDate格式错误
        if (DateUtil.isBefore4LocalDate(queryTxnInfo.getEndDate(), queryTxnInfo.getBeginDate(), DateUtil.DATE_FORMAT_SHORT)) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000053)); //日期范围错误
        }
        if (queryTxnInfo.getPageSize() > SysConstant.PAGE_SIZE_LIMIT_1041) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000055), new Object[]{SysConstant.PAGE_SIZE_LIMIT_1041});// 每页记录数不能超过{0}
        }

        TblCustInf tblCustInf = this.custService.queryCustInfo(queryTxnInfo.getAcctId());
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }
        MemberPointsTxnParams memberPointsTxnParams = new MemberPointsTxnParams();
        memberPointsTxnParams.setCustId(tblCustInf.getCustId());
        memberPointsTxnParams.setPageNum(queryTxnInfo.getPageNum());
        memberPointsTxnParams.setPageSize(queryTxnInfo.getPageSize());
        memberPointsTxnParams.setBeginDate(DateUtil.string2Date4LocalDate(queryTxnInfo.getBeginDate(), DateUtil.DATE_FORMAT_SHORT));
        memberPointsTxnParams.setEndDate(DateUtil.string2Date4LocalDateAddDay(queryTxnInfo.getEndDate(), 1, DateUtil.DATE_FORMAT_SHORT));
        PageInfo<ServiceOrderTxnInfo> pageInfo = this.memberPointsService.queryTxnInfo(memberPointsTxnParams);
        ResTxnInfo resTxnInfo = new ResTxnInfo();
        BeanUtils.copyProperties(pageInfo, resTxnInfo);

        response.setData(resTxnInfo);
        response.setResCode(SysConstant.SYS_SUCCESS);
        return response;
    }
}
