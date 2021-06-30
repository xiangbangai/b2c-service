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
@MapperScan(basePackages = "com.**.db2.mapper", sqlSessionFactoryRef = "sqlSessionFactoryDb2")
@Configuration
public class DataSourceConfigDB2 {

    @Resource
    private DB2Config db2Config;

    @Bean(name = "dataSourceDb2")
    public DataSource dataSource() {
        Properties properties = new Properties();
        properties.put("ifxIFXHOST",db2Config.getIfxIFXHOST());
        properties.put("serverName",db2Config.getServerName());
        properties.put("portNumber",db2Config.getPortNumber());
        properties.put("databaseName",db2Config.getDatabaseName());
        properties.put("user",db2Config.getUser());
        properties.put("password",db2Config.getPassword());
        properties.put("ifxNEWCODESET",db2Config.getIfxNEWCODESET());
        properties.put("ifxIFX_USE_STRENC",db2Config.getIfxIFX_USE_STRENC());
        properties.put("ifxDBDATE",db2Config.getIfxDBDATE());
        properties.put("ifxDB_LOCALE",db2Config.getIfxDB_LOCALE());
        properties.put("ifxCLIENT_LOCALE",db2Config.getIfxCLIENT_LOCALE());

        AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
        atomikosDataSourceBean.setXaDataSourceClassName("com.informix.jdbcx.IfxXADataSource");
        atomikosDataSourceBean.setUniqueResourceName("dataSourceDb2");
        atomikosDataSourceBean.setMinPoolSize(db2Config.getMinPoolSize());
        atomikosDataSourceBean.setMaxPoolSize(db2Config.getMaxPoolSize());
        atomikosDataSourceBean.setMaxLifetime(db2Config.getMaxLifeTime());
        atomikosDataSourceBean.setBorrowConnectionTimeout(db2Config.getBorrowConnectionTimeout());
        atomikosDataSourceBean.setXaProperties(properties);

        return atomikosDataSourceBean;
    }

    @Bean(name = "sqlSessionFactoryDb2")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSourceDb2") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();

        //添加PageHelper插件
        Interceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "informix");
        properties.setProperty("reasonable", "false");
        properties.setProperty("countColumn", "*");
        interceptor.setProperties(properties);

        bean.setPlugins(new Interceptor[]{interceptor,new CatMybatisInterceptor(db2Config.getUrl())});
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "sqlSessionTemplateDb2")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("sqlSessionFactoryDb2") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }



}
