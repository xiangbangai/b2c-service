package com.huateng.web.service;


import com.huateng.base.BaseService;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.data.db2.mapper.*;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.AccountNoRegister;
import com.huateng.data.vo.params.MemberCancellation;
import com.huateng.data.vo.params.VisualCardNo;
import com.huateng.service.remote.QueryCustRemote;
import com.huateng.service.remote.QueryMemberPointsRemote;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MemberOperateService extends BaseService {


    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ApolloBean apolloBean;

    @Resource
    private QueryMemberPointsRemote queryMemberPointsRemote;
    @Resource
    private QueryCustRemote queryCustRemote;


    @Resource
    private TblCustInfMapper tblCustInfMapper;
    @Resource
    private TblBonusPlanMapper tblBonusPlanMapper;
    @Resource
    private TblBonusPlanDetailMapper tblBonusPlanDetailMapper;
    @Resource
    private TblBonusDetailMapper tblBonusDetailMapper;
    @Resource
    private TblTxnDetailMapper tblTxnDetailMapper;
    @Resource
    private SequenceMapper sequenceMapper;
    @Resource
    private TblAcctInfMapper tblAcctInfMapper;



    /**
     * 会员注销
     * @param memberCancellation
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancellation(MemberCancellation memberCancellation) throws Exception{


        String lockName = SysConstant.LOCK_SERVICE_MEMBER + memberCancellation.getCustId();
        RLock rLock = this.redissonClient.getLock(lockName);
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }

            Date txnDate = memberCancellation.getTxnDate();
            String yyyyMMdd = DateUtil.date2String(txnDate, DateUtil.DATE_FORMAT_COMPACT);
            String HHmmss = DateUtil.date2String(txnDate, DateUtil.DATE_FORMAT_TIME);

            //更新会员状态
            TblCustInf dto = new TblCustInf();
            dto.setCustId(memberCancellation.getCustId());//会员id
            dto.setIsActive(SysConstant.MEMBER_CANCELLATION);//账户销户状态
            dto.setCloseDate(yyyyMMdd);//销户日期
            dto.setIsChange(SysConstant.IS_CHANGE);//1有修改
            dto.setModifyDate(yyyyMMdd);
            dto.setModifyTime(HHmmss);
            this.tblCustInfMapper.updateByCustIdSelective(dto);

            //查询会员积分总账
            ResInfo bonusPlanResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(memberCancellation.getCustId(), SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan tblBonusPlan = JacksonUtil.toObject(getResJson(bonusPlanResInfo), TblBonusPlan.class);
            bonusPlanResInfo = null;
            if((tblBonusPlan == null) || (tblBonusPlan.getValidBonus() == null) || (tblBonusPlan.getValidBonus().compareTo(BigDecimal.ZERO) < 1) ){
                log.info("会员{}积分账户无可用积分，无需清零",memberCancellation.getCustId());
                return;
            }

            BigDecimal validBonus = tblBonusPlan.getValidBonus();

            //积分清零以总账积分数为准，有效期分数全部置为0
            TblBonusPlan planDTO = new TblBonusPlan();
            planDTO.setPkBonusPlan(tblBonusPlan.getPkBonusPlan());
            planDTO.setValidBonus(BigDecimal.ZERO);
            planDTO.setApplyBonus(tblBonusPlan.getApplyBonus().add(validBonus));
            this.tblBonusPlanMapper.updatePlanBonus(planDTO);
            planDTO = null;
            log.info("会员{}已清除积分数{}",memberCancellation.getCustId(),validBonus);

            //将有效期分数全部置为0分
            this.tblBonusPlanDetailMapper.bonusCleared(memberCancellation.getCustId());

            String desc = "会员注销，清空会员积分账户剩余可用积分，积分数为"+validBonus;
            String bonusSsn = getSnowId();
            //tbl_bonus_detail
            TblBonusDetail tblBonusDetail = new TblBonusDetail();
            tblBonusDetail.setCustId(memberCancellation.getCustId());
            tblBonusDetail.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
            tblBonusDetail.setTxnType("02");
            tblBonusDetail.setTxnCode(SysConstant.TXN_CODE_OVERDUE);
            tblBonusDetail.setBonusCdFlag(SysConstant.BONUS_FLAG_C);
            tblBonusDetail.setBonusSsn(bonusSsn);
            tblBonusDetail.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            tblBonusDetail.setTxnBonus(validBonus);
            tblBonusDetail.setValidBonus(BigDecimal.ZERO);
            tblBonusDetail.setBpValidBonus(BigDecimal.ZERO);
            tblBonusDetail.setTxnAmtOra(BigDecimal.ZERO);
            tblBonusDetail.setTxnCntOra(BigDecimal.ZERO);
            tblBonusDetail.setTxnDate(yyyyMMdd);
            tblBonusDetail.setTxnTime(HHmmss);
            tblBonusDetail.setCreateDate(yyyyMMdd);
            tblBonusDetail.setCreateTime(HHmmss);
            tblBonusDetail.setStlmDate(yyyyMMdd);
            tblBonusDetail.setDetailDesc(desc);
            this.tblBonusDetailMapper.insert(tblBonusDetail);

            //tbl_txn_detail
            TblTxnDetail txnDetail = new TblTxnDetail();
            txnDetail.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
            txnDetail.setBonusSsn(bonusSsn);
            txnDetail.setChnlNo(SysConstant.SYSTEM_CHANNEL_BPS);
            txnDetail.setTxnDate(yyyyMMdd);
            txnDetail.setTxnTime(HHmmss);
            txnDetail.setTxnType("02");
            txnDetail.setTxnCode(SysConstant.TXN_CODE_OVERDUE);
            txnDetail.setTxnDesc(desc);
            txnDetail.setCustId(memberCancellation.getCustId());
            txnDetail.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            txnDetail.setTxnBonus(validBonus);
            txnDetail.setBonusCdFlag(SysConstant.BONUS_FLAG_C);
            txnDetail.setTxnStatus("0");
            txnDetail.setCreateDate(yyyyMMdd);
            txnDetail.setCreateTime(HHmmss);
            this.tblTxnDetailMapper.insert(txnDetail);
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
        }
    }

    /**
     * 会员注册-主账户
     * @param accountNoRegister
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public VisualCardNo accountNoRegister(AccountNoRegister accountNoRegister) throws Exception{
        String lockName = SysConstant.LOCK_SERVICE_MEMBER + accountNoRegister.getAccountNo();
        RLock rLock = this.redissonClient.getLock(lockName);
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }

            VisualCardNo visualCardNo = new VisualCardNo();

            //校验该主账户是否已创建
            ResInfo resInfo = this.queryCustRemote.queryCustInfo(accountNoRegister.getAccountNo());
            TblCustInf tblCustInf = JacksonUtil.toObject(getResJson(resInfo), TblCustInf.class);
            if(tblCustInf != null){
                log.info("主账户注册重复请求返回创建成功的会员号,{}主账户生成的会员号为{}",accountNoRegister.getAccountNo(),tblCustInf.getCustId());
                visualCardNo.setVisualCardNo(tblCustInf.getCustId());
                return visualCardNo;
            }

            //生成会员号
            String custId = this.genCardNo(SysConstant.VISUAL_CARD_PREFIX);
            log.info("{}主账户生成的会员号为{}",accountNoRegister.getAccountNo(),custId);

            String yyyyMMdd = DateUtil.getCurrentDateyyyyMMdd();
            String HHmmss = DateUtil.getCurrentDateHHmmss();

            TblCustInf custInf = new TblCustInf();
            custInf.setCustId(custId);
            custInf.setCustType(SysConstant.CUST_TYPE_ENTERPRISE); //null V 为个人；E 为企业会员
            custInf.setOpenDate(yyyyMMdd);
            custInf.setModifyDate(yyyyMMdd);
            custInf.setModifyTime(HHmmss);
            custInf.setIsActive(SysConstant.MEMBER_ACTIVE);//激活用户
            //custInf.setIsAuthMobile("Y");//已验证手机
            //custInf.setIsChange(0); //是否修改 默认0-没修改、1-有修改
            //custInf.setIsAcceptEinvoice(0);//是否接收自动开电子发票 0-不接受、1-接受
            //custInf.setIsRealCust(0);//是否真我会员  默认0-不是、1-是
            //custInf.setIsMobileUse(0);//是否使用手机/会员号交易或使用电子券  默认0-不接受、1-接受
            //custInf.setLimitedCount(0);
            custInf.setIsPlateNumPay(0);//是否支持车牌付 默认0-不接受、1-接受
            custInf.setLimitedMaxCount(2);//老交易积分次数上限
            custInf.setLimitedType("01");//限制类型
            custInf.setIsCustAgreement(accountNoRegister.getIsCustAgreement());
            custInf.setAgreementTime(DateUtil.date2String(DateUtil.string2Date4LocalDateTime(accountNoRegister.getAgreementTime(),DateUtil.DATE_FORMAT_FULL),DateUtil.DATE_FORMAT_COMPACT_FULL));
            tblCustInfMapper.insertSelective(custInf);

            TblAcctInf acctInf = new TblAcctInf();
            acctInf.setCustId(custId);
            acctInf.setAcctId(custId);
            acctInf.setAcctType("88");//会员号
            acctInf.setCreateDate(yyyyMMdd);
            acctInf.setCreateTime(HHmmss);
            tblAcctInfMapper.insertSelective(acctInf);

            TblAcctInf acctInfAccount = new TblAcctInf();
            acctInfAccount.setCustId(custId);
            acctInfAccount.setAcctId(accountNoRegister.getAccountNo());
            acctInfAccount.setAcctType("94");//主账户
            acctInfAccount.setCreateDate(yyyyMMdd);
            acctInfAccount.setCreateTime(HHmmss);
            tblAcctInfMapper.insertSelective(acctInfAccount);


            visualCardNo.setVisualCardNo(custId);
            return visualCardNo;
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
        }
    }

    /**
     *<p><strong>Description:</strong>生成卡号</p>
     *<p>
     *虚拟卡用10位数字编码：
     *1-2位是分类码，普通客户88，第3位是校验码（校验码是根据其他7位码计算的结果，避免顺序猜码漏洞），4-10位为客户顺序号。
     *预留0000001-0800000号码段，可以后台指配这个号段内特殊号码给VIP客户。
     *虚拟卡号举例：88 6 0800001
     *</p>
     * @param prefix 卡号前缀
     * @return
     * @author 王飞
     * @update 日期: 2015年7月30日
     */
    private String genCardNo(String prefix) {
        StringBuilder cardNo = new StringBuilder(prefix);
        long seqno = this.sequenceMapper.getSeq(SysConstant.SEQ_CARDNO).longValue();
        String seqCardno = String.valueOf(seqno);
        seqCardno = StringUtils.leftPad(seqCardno, 9, "0");
        cardNo.append(genChecksum(seqCardno));
        cardNo.append(seqCardno);
        return cardNo.toString();
    }

    /**
     *<p><strong>Description:</strong><br />根据传入的卡号字符串生成校验位值<br />生成算法与FCS系统相同</p>
     * @param cardNo
     * @return
     * @author 王飞
     * @update 日期: 2015年7月30日
     */
    private int genChecksum(String cardNo) {
        int sum = 0;
        for (int i = 1; i<cardNo.length()+1; i++) {
            int v = Integer.valueOf(cardNo.substring(i-1, i));
            if (i%2 != 0) {
                sum += v;
            } else {
                sum += v * 3;
            }
        }
        int m = sum/10;
        int res =  (10-(sum - m*10));
        if(res<0||res>9){
            res=0;
        }
        return res;
    }
}
