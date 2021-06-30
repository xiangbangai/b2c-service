package com.huateng.web.service;

import com.huateng.common.util.BusinessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/14
 * Time: 20:52
 * Description: 公共校验方法
 */
@Service
public class ValidateService {

    @Resource
    private RedisService redisService;

    /**
     * 验证字符串是否为空
     * @param str 字符串
     * @param errorCode 错误码
     * @throws Exception
     */
    public void validateForString(String str, String errorCode) throws Exception {
        if(null == str || "".equals(str)) {
            throw new BusinessException(this.redisService.getErrorInfo().get(errorCode));
        }
    }

    /**
     * 验证数值是否为空
     * @param o 数值
     * @param errorCode 错误码
     * @throws Exception
     */
    public void validateForObject(Object o,String errorCode)throws Exception{
        if(null == o){
            throw new BusinessException(this.redisService.getErrorInfo().get(errorCode));
        }
    }

    /**
     * 校验时间格式
     * @param date 日期字符串
     * @param pattern 格式化格式
     * @param errorCode 错误码
     * @throws Exception
     */
    public void validateForDateTime(String date, String pattern,  String errorCode) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalDateTime.parse(date, formatter);
        } catch (Exception e) {
            throw new BusinessException(this.redisService.getErrorInfo().get(errorCode));
        }
    }

    /**
     * 校验时间格式
     * @param date 日期字符串
     * @param pattern 格式化格式
     * @param errorCode 错误码
     * @throws Exception
     */
    public void validateForDate(String date, String pattern,  String errorCode) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalDate.parse(date, formatter);
        } catch (Exception e) {
            throw new BusinessException(this.redisService.getErrorInfo().get(errorCode));
        }
    }

    /**
     * 校验集合
     * @param value
     * @param errorCode
     * @throws Exception
     */
    public void validateForArrayList(List value, String errorCode)throws Exception{
        if(value == null ||  value.isEmpty()){
            throw new BusinessException(this.redisService.getErrorInfo().get(errorCode));
        }
    }
}
