package com.huateng.web.service;

import com.huateng.common.util.BusinessException;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.config.feign.JwtUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/13
 * Time: 20:50
 * Description: aop切面处理逻辑
 */
@Service
public class AopDoSomethingService {

    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private RedisService redisService;

    /**
     * @User Sam
     * @Date 2019/9/13
     * @Time 21:11
     * @Param
     * @return
     * @Description 验证token
     */
    public void validateToken() throws Exception {
        String token = this.httpServletRequest.getHeader(SysConstant.TOKEN);
        if (!this.jwtUtil.validateToken(token)) {
            /**通讯异常,token验证失败或者超时**/
            throw new BusinessException(this.redisService.getErrorInfo().get(SysConstant.SYS_TOKEN_ERROR));
        }
    }

}
