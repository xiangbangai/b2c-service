package com.huateng.common.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/12
 * Time: 22:19
 * Description:
 */
public class SysConstant {

    private SysConstant() {
    }

    public static final String TOKEN = "token";
    private static final String JWT_ALG = "alg";
    private static final String JWT_TYP = "typ";
    private static final String JWT_HS512 = "HS512";
    private static final String JWT = "JWT";
    public static final Map<String, Object> JWT_MAP = new HashMap<>();
    static {
        JWT_MAP.put(JWT_ALG, JWT_HS512);
        JWT_MAP.put(JWT_TYP, JWT);
    }
    public static final String START_WITH_ERROR = "E";
    /**
     * 用于本次请求的签名.
     */
    public static final String X_HMAC_AUTH_SIGNATURE = "x-hmac-auth-signature";

    /**
     * 用户请求签名的时间戳.
     */
    public static final String X_HMAC_AUTH_DATE = "x-hmac-auth-date";

    /**
     * 用于请求签名的签名方法.默认为 HmacSHA256.
     */
    public static final String X_HMAC_AUTH_METHOD = "x-hmac-signature-method";
    public static final String SEND_WECHAT_ERROR = "官微接口调用异常";
    public static final String LOCK_SPLIT = "_";

    /**********************************redisson*********************************************/
    public static final String REDISSON_CONFIG_FILE_NAME = "redisson-{0}.json";
    public static final String REDISSON_UPATE_PASSWORD = "password";
    public static final String REDISSON_CONFIG_CLUSTER = "clusterServersConfig";
    public static final String REDISSON_CONFIG_SINGLE = "singleServerConfig";

    /**cat**/
    public static final String FILTER_PATTERNS = "/*";
    public static final String FILTER_NAME = "cat-filter";
    public static final String CAT_SQL = "SQL";
    public static final String CAT_DATABASE = "SQL.Database";
    public static final String CAT_METHOD = "SQL.Method";
    public static final String CAT_SYSTEM_ERROR = "SystemError";
    public static final String CAT_BUSINESS_ERROR = "BusinessError";



    /***apollo**/
    public static final String APOLLO_START_KEY_B2C = "b2c.service";
    public static final String APOLLO_BEAN_APOLLO_BEAN = "apolloBean";
    public static final String APOLLO_START_KEY_YML = "b2c.yml";
    public static final String APOLLO_START_CONTROL = "b2c.control";
    public static final String APOLLO_BEAN_APOLLO_YML = "apolloYML";
    public static final String APOLLO_BEAN_CONTROL = "apolloControl";


    /**params**/
    public static final String JSON_CONTENTTYPE = "application/json;charset=UTF-8";
    public static final String HTTP_HEAD_SIGNATURE = "signature"; //http签名字段
    public static final String HTTP_HEAD_PROXY_SIGNATURE = "proxySignature"; //代理签名
    public static final String HTTP_HEAD_CHANNEL = "channel"; //渠道
    public static final String HTTP_HEAD_PROXY_CHANNEL = "proxyChannel"; //代理渠道
    public static final String JSON_STR = "jsonStr"; //json字符串
    public static final String JSON_OBJECT = "jsonObj"; //json对象
    public static final String REQ_DATA = "data";
    public static final String YEAR_LAST_DAY = "1230";
    public static final String INTEGRAL_NAME = "标准积分";
    public static final BigDecimal BONUS_COST = new BigDecimal("0.2");


    /**redis本地key**/
    public static final String REDIS_ERROR_CODE = "errorCode";
    public static final String REDIS_ERROR_CODE_VALUE = "errorValue";
    public static final String REDIS_DICT_CODE = "dictCode";
    public static final String REDIS_DICT_CODE_VALUE = "dictValue";
    public static final String REDIS_CHANNEL_KEY = "channelKey";
    public static final String REDIS_CHANNEL_KEY_VALUE = "channelValue";
    public static final String REDIS_RULE_INFO_KEY = "ruleInfoKey";
    public static final String REDIS_RULE_INFO_KEY_VALUE = "ruleInfoValue";
    public static final String REDIS_LOG_TOKEN_KEY = "logTokenKey";
    public static final String REDIS_LOG_TOKEN_KEY_VALUE = "logTokenValue";
    public static final String REDIS_INTERFACE_KEY = "interfaceKey";
    public static final String REDIS_INTERFACE_KEY_VALUE = "interfaceValue";
    public static final String REDIS_NOT_PRODUCE_MIDTYPE_KEY = "noPointsMidTypeKey";
    public static final String REDIS_NOT_PRODUCE_MIDTYPE_KEY_VALUE = "noPointsMidTypeValue";

    /**dict字典key**/
    public static final String DICT_KEY_1001000 = "1001000"; //禁止手机查询渠道
    public static final String DICT_KEY_1002000 = "1002000"; //系统参数
    public static final String DICT_KEY_1003000 = "1003000"; //指定折扣不允许积分
    public static final String DICT_KEY_1004000 = "1004000"; //微信推送模板
    public static final String DICT_KEY_1005000 = "1005000"; //积分相关
    public static final String DICT_KEY_1006000 = "1006000"; //客户级别优惠系数
    public static final String DICT_KEY_POINTS_CHANGE = "memberPointsChange"; //积分变动
    public static final String DICT_KEY_POINTS_ADJUST = "memberPointsAdjust"; //积分调整
    public static final String DICT_KEY_RULE_CARD_BIN = "ruleCardBin"; //自定义卡BIN
    public static final String DICT_KEY_RULE_CARD_BIN_RANGE = "ruleCardBinRange";//卡BIN截取范围
    public static final String DICT_BONUS_DELAY = "bonusDelay";//积分待发



    /**业务参数**/
    /***********************************积分*******************************************/
    public static final Short CUSTOMER_LIMIT_TYPE_DAY = 1; //积分日累计
    public static final Short ORDER_STATUS_PROCESS = 0; //订单状态-处理中
    public static final Short ORDER_STATUS_SUCCESS = 1; //订单状态-成功
    public static final Short ORDER_STATUS_FAILURE = 2; //订单状态-失败
    public static final Short ORDER_STATUS_REVERSAL_PART = 3; //订单状态-部分冲正
    public static final Short ORDER_STATUS_REVERSAL_FULL = 4; //订单状态-全部冲正
    public static final short[] ORDER_STATUS_SUCCESS_INCLUDE = {ORDER_STATUS_SUCCESS, ORDER_STATUS_REVERSAL_PART, ORDER_STATUS_REVERSAL_FULL}; //成功交易包含状态
    public static final Short ORDER_TYPE_PRODUCE = 0; //订单类型-积分产生
    public static final Short ORDER_TYPE_EXCHANGE = 1; //订单类型-积分兑换
    public static final Short ORDER_TYPE_RETURN = 2; //订单类型-积分冲正
    public static final Short ORDER_TYPE_REISSUE = 3; //订单类型-积分调整
    public static final short[] CAN_REVERSAL_STATUS = {ORDER_STATUS_SUCCESS, ORDER_STATUS_REVERSAL_PART};//积分冲正可充原单状态--数组必须保证从小到大有序
    public static final String[] OLD_CAN_REVERSAL_TYPE = {"3011","3021"};//积分冲正支持的原单类型-老交易
    public static final String BP_PLAN_TYPE_DEFAULT = "1111"; //默认的积分计划
    public static final Integer ADD_POINTS_TYPE_REQUEST = 0; //产生积分
    public static final Integer ADD_POINTS_TYPE_REPAIR = 1; //补传
    public final static String BP_LOCK_STATUS_LOCKED = "1";//积分计划冻结状态
    public final static String YEAR_FLAG = "1230";//年底标识
    public final static Short GOODS_TYPE_NOT_OIL = 0;// 商品类型-非油品
    public final static Short GOODS_TYPE_OIL = 1;// 商品类型-油品
    public final static Short GOODS_TYPE_REISSUE = 2;// 商品类型-积分调整
    public final static Short GOODS_TYPE_MANUAL = 3;// 商品类型-手工录入油品
    public final static Short BONUS_OPERATE_INCREASE = 1;//积分增加操作
    public final static Short BONUS_OPERATE_DECREASE = 0;//积分减少操作
    public final static String BONUS_FLAG_D = "d";//老交易增减标识-增加
    public final static String BONUS_FLAG_C = "c";//老交易增减标识-减少
    public final static String PAY_TYPE_UNIONPAY = "1";//支付方式为银联支付
    public final static String NOT_OIL_FLAG = "nonFuel";//非油品标识
    public final static String AUTO_REISSUE_BONUS_DELAY = "0";//积分自动补发-数据处理状态-未处理
    public final static String SYSTEM_CHANNEL = "56"; //积分系统渠道号
    public final static String SYSTEM_CHANNEL_BPS = "BPS"; //积分系统渠道号
    public final static String TXN_CODE_OVERDUE = "9002"; //积分过期业务代码
    public final static String SYSTEM_STATION = "9988"; //总部油站编号
    public final static String DEFAULT_USAGE_KEY = "JF"; //默认的usageKey
    public final static String BONUS_PLAN_LOCK_STATUS_NORMAL = "0"; //积分账户状态-正常
    public final static String BONUS_PLAN_LOCK_STATUS_LOCK = "1"; //积分账户状态-锁定
    public final static String DEFAULT_CREATE_OPER = "system"; //默认的账户创建人
    public final static Short STATUS_UNDELIVERY = 0; //未提货状态
    public final static String BONUS_PLAN_DETAIL_LOCK_STATUS_NORMAL = "0"; //积分有效期状态-正常
    public final static String BONUS_PLAN_DETAIL_LOCK_STATUS_LOCK = "1"; //积分有效期状态-锁定
    public final static String BONUS_PLAN_DETAIL_IN_EFFECT = "0"; //积分明细生效
    public final static String BONUS_PLAN_DETAIL_EXPIRED = "1"; //积分明细已过期
    public final static Short REVERSE_TYPE_All = 0; //0=整单冲正，1=部分冲正
    public final static String MEMBER_POINTS_MSG_TITLE = "积分变动"; //消息标题
    public final static String MEMBER_POINTS_EXCHANGE_TYPE = "支出"; //积分支出
    public final static String MEMBER_POINTS_PRODUCE_TYPE = "获得"; //积分支出
    public final static String MEMBER_POINTS_INCREASE_TYPE = "增加"; //积分增加
    public final static String MEMBER_POINTS_DECREASE_TYPE = "减少"; //积分减少
    public final static Integer SEND_WECHAT_MSG_EXCHANGE = 0; //积分兑换消息类型
    public final static Integer SEND_WECHAT_MSG_PRODUCE = 1; //积分产生消息类型
    public final static Integer SEND_WECHAT_MSG_ADJUST = 2; //积分调整消息类型
    public final static Integer SEND_WECHAT_MSG_EXTERNAL_ADJUST = 3; //外部系统积分调整消息类型
    public final static Integer PAGE_SIZE_LIMIT_1041 = 50; //1041最大pageSize
    public final static String WORK_DATE_TIME = " 00:00:0";
    public final static String WORK_DATE= "1900-01-01 ";
    public final static String CUST_LABEL_TYPE_2 = "2";
    public final static String CUST_LABEL_STATUS_01 = "01";
    public final static Short INVOICE_SEND_SUCCESS = 1;
    public final static Short INVOICE_SEND_FAIL = 0;
    public final static String START_WITH_MEMBER = "88"; //会员88开头
    public final static String COUPON_POINTS_MSG_TITLE = "电子券赠送"; //电子券赠送

    /***********************************电子券*******************************************/
    public final static String CARD_ACTIVITY_IS_OK = "1"; //电子券活动有效状态
    public final static String CARD_USER_STATUS_0 = "0"; //电子券状态可用
    public final static String TXN_CODE_RETURN_COUPON = "6003";//冲正电子券
    public final static String OIL_COUPON = "0"; //油品券
    public final static String OIL_COUPON_LIMIT = "oil_coupon_limit"; //油品券使用数量限制
    public final static String NONFUEL_COUPON_LIMIT = "nonFuel_coupon_limit"; //非油券使用数量限制
    public final static String OIL_COUPON_LIMIT_KEY = "b2c:key:limit:oilcoupon.";
    public final static String NONFUEL_COUPON_LIMIT_KEY = "b2c:key:limit:nonfuelcoupon.";
    public final static String TXN_CODE_USE_COUPON = "6002";//消费电子券

    /***********************************log*******************************************/
    public final static Short LOG_STATUS_ON = 1;
    public final static Short LOG_TOKEN_OPEN = 1;
    public final static String RESULT_SUCCESS = "成功";
    public final static String RESULT_FAIL = "失败";
    public final static String SPLIT_TYPE1 = "&";
    public final static String SPLIT_TYPE2 = "=";
    public final static String SPLIT_TYPE3 = ".";
    public final static Short UPDATE_REDIS_TOKEN = 1;
    public final static Short TASK_UPDATE_REDIS_TOKEN = 0;


    /***********************************会员操作*******************************************/
    public final static String MEMBER_CANCELLATION = "N";//会员注销状态
    public final static String MEMBER_ACTIVE = "Y";//会员启用状态
    public final static Integer IS_CHANGE = 1;//有改动
    public final static String VISUAL_CARD_PREFIX = "88";//虚拟卡前缀
    public final static String SEQ_CARDNO = "seq_cardno";//卡号序列
    public final static String CUST_TYPE_ENTERPRISE = "E";//企业客户类型
    public final static String IS_DEFAULT_LIMIT = "IS_DEFAULT_LIMIT"; //会员默认积分上限
    public final static String IS_DEFAULT_LIMIT_TYPE = "IS_DEFAULT_LIMIT_TYPE"; //会员积分次数上限类型
    public final static Integer SEND_THRE_SHOLD = 90; //注销时间与注册时间送券阀值天数
    public final static String IS_DEFAULT_ACCT_TYPE = "acctType"; //注销时间与注册时间送券阀值天数
    public final static String PHONE_ACCT_TYPE = "89"; //手机类型
    public final static String CUST_ACCT_TYPE = "88"; //会员号类型
    public final static String OPEN_ACCT_TYPE = "90"; //微信openId类型
    public final static String VISUAL_ACCT_TYPE = "04"; //虚拟卡

    /*************************************lock**********************************************/
    public final static String LOCK_MEMBER_POINTS = "b2c:lock:bonus."; //全局积分锁,积分操作单位时间只允许操作一个会员
    public final static String LOCK_SERVICE_MEMBER = "b2c:lock:member."; //全局会员锁,会员操作单位时间只允许操作一个会员
    public final static String LOCK_SERVICE_COUPON = "b2c:lock:coupon."; //全局电子券锁,电子券操作单位时间只允许操作一张电子券
    public final static String LOCK_SERVICE_ORDER = "b2c:lock:serviceOrder."; //service_order表流水锁，确保不会有重复交易 channel+reqSerialNo+orderType
    /*************************************redis前缀**********************************************/
    public final static String REDIS_KEY_QRCODE = "b2c:key:qrcode.";




    /**
     * 8位错误码，前2位区分不同模块：
     * 90: 微服务本身,b2c-servide-query
     * 01: b2c-online
     * 02: b2c-member-points
     */
    public static final String SYS_ERROR_START_WITH = "E99";
    public static final String SYS_ERROR = "E99999999"; //系统繁忙
    public static final String SYS_SUCCESS = "SUCCESS"; //成功
    public static final String SYS_REDIS_ERROR = "E99999998"; //redis缓存获取失败
    public static final String SYS_REDIS_ERROR_MSG = "REDIS缓存异常"; //redis缓存异常
    public static final String SYS_TOKEN_ERROR = "E99999997"; //token验证失败
    public static final String SYS_LOCK_ERROR = "E99999996"; //获取锁失败
    public static final String SYS_PARAMS_NULL = "E99999995"; //请求参数为空




    /***********************************online错误码*******************************************/
    public static final String E01000001 = "E01000001"; //渠道错误
    public static final String E01000002 = "E01000002"; //解析报文失败
    public static final String E01000003 = "E01000003"; //acctId不可为空
    public static final String E01000004 = "E01000004"; //pageSize不可为空
    public static final String E01000005 = "E01000005"; //当前渠道不允许手机查询会员信息
    public static final String E01000006 = "E01000006"; //代理渠道错误
    public static final String E01000007 = "E01000007"; //渠道关联错误
    public static final String E01000008 = "E01000008"; //代理渠道验签失败
    public static final String E01000009 = "E01000009"; //渠道验签失败
    public static final String E01000010 = "E01000010"; //custId不可为空
    public static final String E01000011 = "E01000011"; //pageNum不可为空
    public static final String E01000012 = "E01000012"; //reqSerialNo不可为空
    public static final String E01000013 = "E01000013"; //businessDate格式错误
    public static final String E01000014 = "E01000014"; //reqDateTime格式错误 yyyy-MM-dd HH:mm:ss
    public static final String E01000015 = "E01000015"; //requestType格式错误
    public static final String E01000016 = "E01000016"; //businessDate格式错误
    public static final String E01000017 = "E01000017"; //payment不可为空
    public static final String E01000018 = "E01000018"; //多油品开关错误
    public static final String E01000019 = "E01000019"; //手工录入油品开关错误
    public static final String E01000020 = "E01000020"; //{0}折扣不允许积分
    public static final String E01000021 = "E01000021"; //会员未同意服务协议，不允许积分
    public static final String E01000022 = "E01000022"; //积分实体卡不允许积分
    public static final String E01000023 = "E01000023"; //goodsId不可为空
    public static final String E01000024 = "E01000024"; //id不可为空
    public static final String E01000025 = "E01000025"; //totalPrice不可为空
    public static final String E01000026 = "E01000026"; //unitPrice不可为空
    public static final String E01000027 = "E01000027"; //middleType不可为空
    public static final String E01000028 = "E01000028"; //number不可为空
    public static final String E01000029 = "E01000029"; //goodsName不可为空
    public static final String E01000030 = "E01000030"; //goodsType不可为空
    public static final String E01000031 = "E01000031"; //手工录入油品不允许积分
    public static final String E01000032 = "E01000032"; //多油品不允许积分
    public static final String E01000033 = "E01000033"; //商品列表id重复
    public static final String E01000034 = "E01000034"; //repairSerialNo必须为空
    public static final String E01000035 = "E01000035"; //goods不可为空
    public static final String E01000036 = "E01000036"; //会员不存在
    public static final String E01000037 = "E01000037"; //deliveryDate格式错误
    public static final String E01000038 = "E01000038"; //repairSerialNo不可为空
    public static final String E01000039 = "E01000039"; //posId不可为空
    public static final String E01000040 = "E01000040"; //stationId不可为空
    public static final String E01000041 = "E01000041"; //订单状态异常，请及时报障
    public static final String E01000042 = "E01000042"; //payType不可为空
    public static final String E01000043 = "E01000043"; //payInfo不可为空
    public static final String E01000044 = "E01000044"; //不允许手机号作为凭证
    public static final String E01000045 = "E01000045"; //冲正类型错误
    public static final String E01000046 = "E01000046"; //detail不可为空
    public static final String E01000047 = "E01000047"; //targetSerialNo不可为空
    public static final String E01000048 = "E01000048"; //bonusNumber不可为空
    public static final String E01000049 = "E01000049"; //原定单状态错误
    public static final String E01000050 = "E01000050"; //当前渠道不允许会员号查询会员信息
    public static final String E01000051 = "E01000051"; //beginDate格式错误
    public static final String E01000052 = "E01000052"; //endDate格式错误
    public static final String E01000053 = "E01000053"; //日期范围错误
    public static final String E01000054 = "E01000054"; //交易失败
    public static final String E01000055 = "E01000055"; //每页记录数不能超过{0}
    public static final String E01000056 = "E01000056"; //sale不可为空
    public static final String E01000057 = "E01000057"; //pay不可为空
    public static final String E01000058 = "E01000058"; //pushMsg不可为空
    public static final String E01000059 = "E01000059"; //adjustName不可为空
    public static final String E01000060 = "E01000060"; //property不可为空
    public static final String E01000061 = "E01000061"; //accountNo不可为空
    public static final String E01000062 = "E01000062"; //accountNo格式不正确
    public static final String E01000063 = "E01000063"; //isCustAgreement不可为空
    public static final String E01000064 = "E01000064"; //isCustAgreement非法
    public static final String E01000065 = "E01000065"; //agreementTime格式错误
    public static final String E01000066 = "E01000066"; //积分账户查询，没有任何记录
    public static final String E01000067 = "E01000067"; //labelId不能为空
    public static final String E01000068 = "E01000068"; //validDays不能为空
    public static final String E01000069 = "E01000069"; //openId不能为空
    public static final String E01000070 = "E01000070"; //不存在该车辆信息
    public static final String E01000071 = "E01000071"; //cardNo不可为空
    public static final String E01000072 = "E01000072"; //车牌号不可为空
    public static final String E01000073 = "E01000073"; //手机号不可为空
    public static final String E01000074 = "E01000074"; //传入手机号码不正确
    public static final String E01000075 = "E01000075"; //虚拟卡号不可为空
    public static final String E01000076 = "E01000076"; //原实体卡号不可为空
    public static final String E01000077 = "E01000077"; //新实体卡号不可为空
    public static final String E01000078 = "E01000078"; //unionId已被{0}会员号注册
    public static final String E01000079 = "E01000079"; //openId已被{0}会员号注册
    public static final String E01000080 = "E01000080"; //alipayUid已被{0}会员号注册
    public static final String E01000081 = "E01000081"; //personalCardNo已被{0}会员号注册
    public static final String E01000082 = "E01000082"; //cardCode不能为空
    public static final String E01000083 = "E01000083"; //活动不存在
    public static final String E01000084 = "E01000084"; //账号类型为空
    public static final String E01000085 = "E01000085"; //券码为空
    public static final String E01000086 = "E01000086"; //发行方编码为空
    public static final String E01000087 = "E01000087"; //couponId不能为空
    public static final String E01000088 = "E01000088"; //acctType类型错误
    public static final String E01000089 = "E01000089"; //acctId已被{0}会员号注册
    public static final String E01000090 = "E01000090"; //当前会员号已存在当前类型的账号

    /***************************************query错误码***************************************/
    public static final String E90000001 = "E90000001"; //二维码已过期
    public static final String E90000002 = "E90000002"; //二维码重复使用
    public static final String E90000003 = "E90000003"; //beginDate格式错误
    public static final String E90000004 = "E90000004"; //返回记录与预期不符,请尽快报障
    public static final String E90000005 = "E90000005"; //二维码不能为空
    public static final String E90000006 = "E90000006"; //用户属性为空
    public static final String E90000007 = "E90000007"; //会员号为空
    public static final String E90000008 = "E90000008"; //积分类型为空
    public static final String E90000009 = "E90000009"; //endDate格式错误
    public static final String E90000010 = "E90000010"; //pageNum不可为空
    public static final String E90000011 = "E90000011"; //pageSize不可为空
    public static final String E90000012 = "E90000012"; //参数为空
    public static final String E90000013 = "E90000013"; //车牌号为空
    public static final String E90000014 = "E90000014"; //手机号为空
    public static final String E90000015 = "E90000015"; //openId不能为空
    public static final String E90000016 = "E90000016"; //labelId不能为空
    public static final String E90000017 = "E90000017"; //couponId不能为空

    /***************************************memberPoints错误码***************************************/
    public static final String E02000001 = "E02000001"; //商品单价与总价不符
    public static final String E02000002 = "E02000002"; //积分账户与明细不符
    public static final String E02000003 = "E02000003"; //兑换总积分小于等于0
    public static final String E02000004 = "E02000004"; //积分余额不足
    public static final String E02000005 = "E02000005"; //客户已经被冻结
    public static final String E02000006 = "E02000006"; //channel不可为空
    public static final String E02000007 = "E02000007"; //冲正积分数不可大于原单可退积分数
    public static final String E02000008 = "E02000008"; //冲正积分与冲正数量不匹配
    public static final String E02000009 = "E02000009"; //原单已无可退积分
    public static final String E02000010 = "E02000010"; //子订单号[{0}]冲正数量已超出原子订单可退数量
    public static final String E02000011 = "E02000011"; //原订单中无{0}子订单号
    public static final String E02000012 = "E02000012"; //日累计开关错误
    public static final String E02000013 = "E02000013"; //会员{0}积分日累计次数超出{1}次限制
    public static final String E02000014 = "E02000014"; //orderType不可为空



    /***************************************member错误码***************************************/
    public static final String E04000001 = "E04000001"; //参数不可为空
    public static final String E04000002 = "E04000002"; //会员号不可为空
    public static final String E04000003 = "E04000003"; //会员不存在
    public static final String E04000004 = "E04000004"; //会员卡号不可为空
    public static final String E04000005 = "E04000005"; //手机号已经被占用
    public static final String E04000006 = "E04000006"; //支付卡已经超过绑定上限
    public static final String E04000007 = "E04000007"; //卡号已绑定其他会员卡
    public static final String E04000008 = "E04000008"; //原手机号不正确
    public static final String E04000009 = "E04000009"; //标签编码不可为空
    public static final String E04000010 = "E04000010"; //有效天数不可为空
    public static final String E04000011 = "E04000011"; //会员标签已存在
    public static final String E04000012 = "E04000012"; //会员标签不存在
    public static final String E04000013 = "E04000013"; //您未绑定实体卡
    public static final String E04000014 = "E04000014"; //找不到新卡信息
    public static final String E04000015 = "E04000015"; //卡号已经被使用
    public static final String E04000016 = "E04000016"; //手机号不可为空
    public static final String E04000017 = "E04000017"; //电子券码不可为空

    /***************************************coupon错误码***************************************/
    public static final String E05000001 = "E05000001"; //参数不可为空
    public static final String E05000002 = "E05000002"; //会员号不可为空
    public static final String E05000003 = "E05000003"; //电子券不存在
    public static final String E05000004 = "E05000004"; //电子券已绑定会员
    public static final String E05000005 = "E05000005"; //电子券活动状态不可用
    public static final String E05000006 = "E05000006"; //电子券未锁定
    public static final String E05000007 = "E05000007"; //电子券锁定流水不一致
    public static final String E05000008 = "E05000008"; //电子券码不可为空
    public static final String E05000009 = "E05000009"; //标签号不可为空
    public static final String E05000010 = "E05000010"; //电子券未通过预核销，不允许使用
    public static final String E05000011 = "E05000011"; //电子券已使用
    public static final String E05000012 = "E05000012"; //找不到客户
    public static final String E05000013 = "E05000013"; //客户每日用券已超出系统限制

    /***************************************log错误码***************************************/
    public static final String E03000001 = "E03000001"; //日志上送异常
}
