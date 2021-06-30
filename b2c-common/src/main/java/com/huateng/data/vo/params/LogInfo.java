package com.huateng.data.vo.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/8/3
 * Time: 11:10
 * Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogInfo {

    //private String token;
    @Builder.Default
    private String relationId = "";
    private String channel; //渠道
    private String accessDay; //数据修改日期
    private Long accessTime; //数据修改时间
    private String responseTime; //单笔耗时
    private String reqSerialNo; //请求流水
    private String resSerialNo; //返回流水
    private String resCode; //返回码
    @Builder.Default
    private String sysCode = "B2C"; //系统编码
    @Builder.Default
    private String sysName = "B2C服务拆分"; //系统名称
    @Builder.Default
    private String userName = ""; //用户名
    @Builder.Default
    private String userCode = ""; //用户编码
    private String reqIp; //请求ip
    private String url; //请求地址
    private String params; //请求参数
    private String actionName; //接口名称
    private String result; //结果成功或失败
    private String returnValue; //返回报文
    @Builder.Default
    private String source = "3"; //来源，1：PC，2：微信，3：接口调用 4：portal
    @Builder.Default
    private String operaType = "3"; //操作类型，1：登陆 2：注销 3:操作
    @Builder.Default
    private String isNoAccess = "2"; //是否非法访问 1：是 2：否
    @Builder.Default
    private String accessType = "2"; //访问类型，1：页面，2：接口
    @Builder.Default
    private String posId = "";
    @Builder.Default
    private String stationId = "";
    @Builder.Default
    private String shiftId = "";
    @Builder.Default
    private String listNo = "";
    @Builder.Default
    private String businessDate = "";
}
