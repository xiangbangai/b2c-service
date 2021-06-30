package com.huateng.config.feign;

import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/12
 * Time: 22:07
 * Description:
 */
@Component
public class JwtUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);

    @Resource
    private ApolloBean apolloBean;
    @Value("${token.secret}")
    private String tokenSecret;

    /**
     * @User Sam
     * @Date 2019/9/12
     * @Time 22:30
     * @Param
     * @return
     * @Description 生成token
     */
    public String getToken() {
        return Jwts.builder()
                .setHeader(SysConstant.JWT_MAP)
                .setExpiration(new Date(System.currentTimeMillis() + this.apolloBean.getTokenTimeout()))
                .signWith(SignatureAlgorithm.HS256, tokenSecret)
                .compact();
    }

    /**
     * @User Sam
     * @Date 2019/9/12
     * @Time 22:35
     * @Param
     * @return
     * @Description 验证token
     */
    public boolean validateToken(String token) {
        Claims claims = null;
        if(token != null) {
            try {
                claims = Jwts.parser()
                        .setSigningKey(tokenSecret)
                        .parseClaimsJws(token)
                        .getBody();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
                return false;
            }
        }
        if (claims != null) {
            return true;
        } else {
            return false;
        }
    }
}
