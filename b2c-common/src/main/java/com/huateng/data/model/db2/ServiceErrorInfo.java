package com.huateng.data.model.db2;

import lombok.Data;

/**
 * Created with b2c-common.
 * User: Sam
 * Date: 2019/9/10
 * Time: 00:01
 * Description:
 */
@Data
public class ServiceErrorInfo {

    private String errorCode;

    private String errorMsg;

    private Integer errorType;
}
