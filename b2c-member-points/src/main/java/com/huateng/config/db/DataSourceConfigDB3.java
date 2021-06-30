package com.huateng.config.db;

import com.github.pagehelper.PageInterceptor;
import com.huateng.config.cat.CatMybatisInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created with coupon.
 * User: Sam
 * Date: 2018/7/18
 * Time: 14:12
 * Description:
 */
@MapperScan(basePackages = "com.**.db3.mapper", sqlSessionFactoryRef = "sqlSessionFactoryDb3")
@Configuration
public class DataSourceConfigDB3 {

    @Resource
    private DB3Config db3Config;

    @Bean(name = "dataSourceDb3")
    public DataSource dataSource() {
        Properties properties = new Properties();
        properties.put("ifxIFXHOST",db3Config.getIfxIFXHOST());
        properties.put("serverName",db3Config.getServerName());
        properties.put("portNumber",db3Config.getPortNumber());
        properties.put("databaseName",db3Config.getDatabaseName());
        properties.put("user",db3Config.getUser());
        properties.put("password",db3Config.getPassword());
        properties.put("ifxNEWCODESET",db3Config.getIfxNEWCODESET());
        properties.put("ifxIFX_USE_STRENC",db3Config.getIfxIFX_USE_STRENC());
        properties.put("ifxDBDATE",db3Config.getIfxDBDATE());
        properties.put("ifxDB_LOCALE",db3Config.getIfxDB_LOCALE());
        properties.put("ifxCLIENT_LOCALE",db3Config.getIfxCLIENT_LOCALE());

        AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
        atomikosDataSourceBean.setXaDataSourceClassName("com.informix.jdbcx.IfxXADataSource");
        atomikosDataSourceBean.setUniqueResourceName("dataSourceDb3");
        atomikosDataSourceBean.setMinPoolSize(db3Config.getMinPoolSize());
        atomikosDataSourceBean.setMaxPoolSize(db3Config.getMaxPoolSize());
        atomikosDataSourceBean.setMaxLifetime(db3Config.getMaxLifeTime());
        atomikosDataSourceBean.setBorrowConnectionTimeout(db3Config.getBorrowConnectionTimeout());
        atomikosDataSourceBean.setXaProperties(properties);

        return atomikosDataSourceBean;
    }

    @Bean(name = "sqlSessionFactoryDb3")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSourceDb3") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();

        //添加PageHelper插件
        Interceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "informix");
        properties.setProperty("reasonable", "false");
        properties.setProperty("countColumn", "*");
        interceptor.setProperties(properties);

        bean.setPlugins(new Interceptor[]{interceptor,new CatMybatisInterceptor(db3Config.getUrl())});
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "sqlSessionTemplateDb3")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("sqlSessionFactoryDb3") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }



}
