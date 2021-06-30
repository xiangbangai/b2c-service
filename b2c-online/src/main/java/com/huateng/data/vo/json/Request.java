package com.huateng.data.vo.json;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/14
 * Time: 21:23
 * Description:
 */
public class Request<T> {

    private String channel;
    private String reqSerialNo;
    private String reqDateTime;
    private Object data;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getReqSerialNo() {
        return reqSerialNo;
    }

    public void setReqSerialNo(String reqSerialNo) {
        this.reqSerialNo = reqSerialNo;
    }

    public String getReqDateTime() {
        return reqDateTime;
    }

    public void setReqDateTime(String reqDateTime) {
        this.reqDateTime = reqDateTime;
    }

    public Object getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
