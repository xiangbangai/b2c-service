package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/6
 * Time: 14:33
 * Description:
 */
@Data
public class SendWeChat {
    private String openId; // 接受消息的用户openId
    private String title; // 消息标题
    private String cardNo; // 会员卡号（实体或虚拟卡号）
    private String centent; // 内容
    private String code;  //油站ID
    private BigDecimal integral; //积分
    private Date date; //主机时间
    private BigDecimal totalPoints; //总积分
    private String adjustFlag;
    private String remark;//广告内容
    private String link;//内容链接
}
