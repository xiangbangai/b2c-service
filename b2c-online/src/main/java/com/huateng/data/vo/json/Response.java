package com.huateng.data.vo.json;

import lombok.Data;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/14
 * Time: 21:23
 * Description:
 */
@Data
public class Response {

    String channel;
    String reqSerialNo;
    String reqDateTime;
    String resCode;
    String resMsg;
    String resSerialNo;
    String resDateTime;
    Object data;
}
