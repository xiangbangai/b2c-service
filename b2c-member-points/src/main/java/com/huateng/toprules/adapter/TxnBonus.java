package com.huateng.toprules.adapter;

import com.huateng.common.util.DateUtil;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * <p>
 * <strong>Description:</strong>
 * </p>
 * 
 * @author ZYK
 * @Company 上海华腾软件系统有限公司
 * @version 1.0
 * @since JDK1.5
 * 
 *        Copyright 2014, Shanghai Huateng Software Systems Co., Ltd. All right
 *        reserved.
 * 
 */
public class TxnBonus {
	private String usage_key ="JF";
	private String txn_date = "";// 交易日期
	private String txn_time = "";// 交易时间
	private String brithday = "";// 生日
	private String txn_week_date = "";// 星期几
	private String txn_month = "";// 交易月份
	private String txn_date_month = "";// 每月几日
	private String cust_id = "";// 客户号
	private String acct_id = "";// 账号
	private String acct_type = "";// 账号类型
	private String station_id = "";// 油站编号
	private String station_name = "";// 油站名称
	private String machine_id = "";// 加油机号
	private String txn_type = "";// 交易类型
	private Double txn_amt = 0d;// 交易金额

//	private String goods_id = "";// 商品编号
	private String goods_name = "";// 商品名称
	private String goods_mid_type = "";// 商品中类
	private Double goods_num = 0d;// 商品数量<-已经不用
	private Double goods_unit_price = 0d;// 商品单价
	private Double goods_act_price = 0d;// 实际购买单价
	private Double goods_act_total_price = 0d;// 实际购买总价

	private String is_include_refueling = "";// 交易包括加油
	private String is_include_goods = "";// 交易包括购买非油商品
	private String channel = "";// 渠道
	private String txn_ssn_ora = "";// 原流水号（前端流水号）
	private String txn_date_ora = "";//原交易日期（前端营业日期）
	
	private Double cust_level_coefficient=0d;//级别系数
	
	
	private String cust_level = "";
	private String is_brith_day = "";
	private String is_brith_month = "";
	private String is_brith_week = "";

	private String goods_lit_type = "";

	private String field1 = ""; // 贷款类型
	private String field2 = ""; // 预留字段
	private String field3 = ""; // 预留字段
	private String field4 = ""; // 预留字段
	private String field5 = ""; // 预留字段

	private String marketregion = "";//市场区域
	
	
	private Double goods_count = 0d;
	private String	goods_ids = "";
	private String pay_type = "";//支付类型
	private String card_type ="";//卡类型
	
	
	private String post_id="";
	
	private Set<String> pay_typeSet = new HashSet<String>();//支付类型一笔交易可能存在多种支付方式
	
	public PackageResult result = new PackageResult();

	public String getTxn_date() {
		return txn_date;
	}

	
	
	public void convertData(){
		//转换生日 当天，当周，当月
		if(brithday!=null&&brithday.length()==8&&txn_date!=null&&txn_date.length()==8){
			String brithdayWD = brithday.substring(4, 8);
			//生日当天
			if(brithdayWD.equals(txn_date.substring(4, 8))){
				is_brith_day="y";
			}
			//生日当月
			if(brithday.substring(4, 6).equals(txn_date.substring(4, 6))){
				is_brith_month="y";
			}
			//生日当周
			is_brith_week=DateUtil.isBirthdayWeek(DateUtil.getCurrentDateyyyy()+brithdayWD,txn_date,DateUtil.DATE_FORMAT_COMPACT);
		}
	}
	
	
	public void setTxn_date(String txn_date){
		txn_week_date = DateUtil.getWeek(txn_date,DateUtil.DATE_FORMAT_COMPACT, DayOfWeek.SUNDAY) + "";
		txn_month = DateUtil.getMonth(txn_date,DateUtil.DATE_FORMAT_COMPACT) + "";
		txn_date_month = DateUtil.getDate(txn_date,DateUtil.DATE_FORMAT_COMPACT) + "";

		this.txn_date = txn_date;
	}

	public String getTxn_time() {
		return txn_time;
	}

	public void setTxn_time(String txn_time) {
		this.txn_time = txn_time;
	}

	public String getBrithday() {
		return brithday;
	}

	public void setBrithday(String brithday) {
		this.brithday = brithday;
	}

	public String getTxn_week_date() {
		return txn_week_date;
	}

	public void setTxn_week_date(String txn_week_date) {
		this.txn_week_date = txn_week_date;
	}

	public String getTxn_month() {
		return txn_month;
	}

	public void setTxn_month(String txn_month) {
		this.txn_month = txn_month;
	}

	public String getTxn_date_month() {
		return txn_date_month;
	}

	public void setTxn_date_month(String txn_date_month) {
		this.txn_date_month = txn_date_month;
	}

	public String getCust_id() {
		return cust_id;
	}

	public void setCust_id(String cust_id) {
		this.cust_id = cust_id;
	}

	public String getAcct_id() {
		return acct_id;
	}

	public void setAcct_id(String acct_id) {
		this.acct_id = acct_id;
	}

	public String getAcct_type() {
		return acct_type;
	}

	public void setAcct_type(String acct_type) {
		this.acct_type = acct_type;
	}

	public String getStation_id() {
		return station_id;
	}

	public void setStation_id(String station_id) {
		this.station_id = station_id;
	}

	public String getStation_name() {
		return station_name;
	}

	public void setStation_name(String station_name) {
		this.station_name = station_name;
	}

	public String getMachine_id() {
		return machine_id;
	}

	public void setMachine_id(String machine_id) {
		this.machine_id = machine_id;
	}

	public String getTxn_type() {
		return txn_type;
	}

	public void setTxn_type(String txn_type) {
		this.txn_type = txn_type;
	}

	public Double getTxn_amt() {
		return txn_amt;
	}

	public void setTxn_amt(Double txn_amt) {
		this.txn_amt = txn_amt;
	}

	/*public String getGoods_id() {
		return goods_id;
	}

	public void setGoods_id(String goods_id) {
		this.goods_id = goods_id;
	}*/

	public String getGoods_name() {
		return goods_name;
	}

	public void setGoods_name(String goods_name) {
		this.goods_name = goods_name;
	}

	public String getGoods_mid_type() {
		return goods_mid_type;
	}

	public void setGoods_mid_type(String goods_mid_type) {
		this.goods_mid_type = goods_mid_type;
	}


	public Double getGoods_num() {
		return goods_num;
	}

	public void setGoods_num(Double goods_num) {
		this.goods_num = goods_num;
	}

	public Double getGoods_unit_price() {
		return goods_unit_price;
	}

	public void setGoods_unit_price(Double goods_unit_price) {
		this.goods_unit_price = goods_unit_price;
	}

	public Double getGoods_act_price() {
		return goods_act_price;
	}

	public void setGoods_act_price(Double goods_act_price) {
		this.goods_act_price = goods_act_price;
	}

	public Double getGoods_act_total_price() {
		return goods_act_total_price;
	}

	public void setGoods_act_total_price(Double goods_act_total_price) {
		this.goods_act_total_price = goods_act_total_price;
	}

	public String getIs_include_refueling() {
		return is_include_refueling;
	}

	public void setIs_include_refueling(String is_include_refueling) {
		this.is_include_refueling = is_include_refueling;
	}

	public String getIs_include_goods() {
		return is_include_goods;
	}

	public void setIs_include_goods(String is_include_goods) {
		this.is_include_goods = is_include_goods;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getTxn_ssn_ora() {
		return txn_ssn_ora;
	}

	public void setTxn_ssn_ora(String txn_ssn_ora) {
		this.txn_ssn_ora = txn_ssn_ora;
	}

	public String getCust_level() {
		return cust_level;
	}

	public void setCust_level(String cust_level) {
		this.cust_level = cust_level;
	}

	public String getIs_brith_day() {
		return is_brith_day;
	}

	public void setIs_brith_day(String is_brith_day) {
		this.is_brith_day = is_brith_day;
	}

	public String getIs_brith_month() {
		return is_brith_month;
	}

	public void setIs_brith_month(String is_brith_month) {
		this.is_brith_month = is_brith_month;
	}

	public String getIs_brith_week() {
		return is_brith_week;
	}

	public void setIs_brith_week(String is_brith_week) {
		this.is_brith_week = is_brith_week;
	}

	public String getGoods_lit_type() {
		return goods_lit_type;
	}

	public void setGoods_lit_type(String goods_lit_type) {
		this.goods_lit_type = goods_lit_type;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

	public String getField3() {
		return field3;
	}

	public void setField3(String field3) {
		this.field3 = field3;
	}

	public String getField4() {
		return field4;
	}

	public void setField4(String field4) {
		this.field4 = field4;
	}

	public String getField5() {
		return field5;
	}

	public void setField5(String field5) {
		this.field5 = field5;
	}

	public PackageResult getResult() {
		return result;
	}

	public void setResult(PackageResult result) {
		this.result = result;
	}

	public String getTxn_date_ora() {
		return txn_date_ora;
	}

	public void setTxn_date_ora(String txn_date_ora) {
		this.txn_date_ora = txn_date_ora;
	}

	public Double getCust_level_coefficient() {
		return cust_level_coefficient;
	}

	public void setCust_level_coefficient(Double cust_level_coefficient) {
		this.cust_level_coefficient = cust_level_coefficient;
	}



	public String getUsage_key() {
		return usage_key;
	}



	public void setUsage_key(String usage_key) {
		this.usage_key = usage_key;
	}



	public String getMarketregion() {
		return marketregion;
	}



	public void setMarketregion(String marketregion) {
		this.marketregion = marketregion;
	}



	public Double getGoods_count() {
		return goods_count;
	}



	public void setGoods_count(Double goods_count) {
		this.goods_count = goods_count;
	}



	public String getGoods_ids() {
		return goods_ids;
	}



	public void setGoods_ids(String goods_ids) {
		this.goods_ids = goods_ids;
	}



	public String getPay_type() {
		return pay_type;
	}



	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}



	public Set<String> getPay_typeSet() {
		return pay_typeSet;
	}



	public void setPay_typeSet(Set<String> pay_typeSet) {
		this.pay_typeSet = pay_typeSet;
	}



	public String getPost_id() {
		return post_id;
	}



	public void setPost_id(String post_id) {
		this.post_id = post_id;
	}



	public String getCard_type() {
		return card_type;
	}



	public void setCard_type(String card_type) {
		this.card_type = card_type;
	}


	@Override
	public String toString() {
		return "TxnBonus{" +
				"usage_key='" + usage_key + '\'' +
				", txn_date='" + txn_date + '\'' +
				", txn_time='" + txn_time + '\'' +
				", brithday='" + brithday + '\'' +
				", txn_week_date='" + txn_week_date + '\'' +
				", txn_month='" + txn_month + '\'' +
				", txn_date_month='" + txn_date_month + '\'' +
				", cust_id='" + cust_id + '\'' +
				", acct_id='" + acct_id + '\'' +
				", acct_type='" + acct_type + '\'' +
				", station_id='" + station_id + '\'' +
				", station_name='" + station_name + '\'' +
				", machine_id='" + machine_id + '\'' +
				", txn_type='" + txn_type + '\'' +
				", txn_amt=" + txn_amt +
				", goods_name='" + goods_name + '\'' +
				", goods_mid_type='" + goods_mid_type + '\'' +
				", goods_num=" + goods_num +
				", goods_unit_price=" + goods_unit_price +
				", goods_act_price=" + goods_act_price +
				", goods_act_total_price=" + goods_act_total_price +
				", is_include_refueling='" + is_include_refueling + '\'' +
				", is_include_goods='" + is_include_goods + '\'' +
				", channel='" + channel + '\'' +
				", txn_ssn_ora='" + txn_ssn_ora + '\'' +
				", txn_date_ora='" + txn_date_ora + '\'' +
				", cust_level_coefficient=" + cust_level_coefficient +
				", cust_level='" + cust_level + '\'' +
				", is_brith_day='" + is_brith_day + '\'' +
				", is_brith_month='" + is_brith_month + '\'' +
				", is_brith_week='" + is_brith_week + '\'' +
				", goods_lit_type='" + goods_lit_type + '\'' +
				", field1='" + field1 + '\'' +
				", field2='" + field2 + '\'' +
				", field3='" + field3 + '\'' +
				", field4='" + field4 + '\'' +
				", field5='" + field5 + '\'' +
				", marketregion='" + marketregion + '\'' +
				", goods_count=" + goods_count +
				", goods_ids='" + goods_ids + '\'' +
				", pay_type='" + pay_type + '\'' +
				", card_type='" + card_type + '\'' +
				", post_id='" + post_id + '\'' +
				", pay_typeSet=" + pay_typeSet +
				'}';
	}
}
