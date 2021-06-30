package com.huateng.config.feign;

import com.dianping.cat.Cat;
import com.huateng.common.util.SysConstant;
import com.huateng.config.cat.CatConstantsExt;
import com.huateng.config.cat.CatContextImpl;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/12
 * Time: 20:55
 * Description:
 */
@Configuration
public class FeignInterceptor implements RequestInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(SysConstant.TOKEN, this.jwtUtil.getToken());

        CatContextImpl catContext = new CatContextImpl();
        Cat.logRemoteCallClient(catContext,Cat.getManager().getDomain());
        requestTemplate.header(CatConstantsExt.CAT_HTTP_HEADER_ROOT_MESSAGE_ID,catContext.getProperty(Cat.Context.ROOT));
        requestTemplate.header(CatConstantsExt.CAT_HTTP_HEADER_PARENT_MESSAGE_ID,catContext.getProperty(Cat.Context.PARENT));
        requestTemplate.header(CatConstantsExt.CAT_HTTP_HEADER_CHILD_MESSAGE_ID,catContext.getProperty(Cat.Context.CHILD));
    }

}
