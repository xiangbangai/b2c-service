package com.huateng.config.db;

import com.github.pagehelper.PageInterceptor;
import com.huateng.config.cat.CatMybatisInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

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
@MapperScan(basePackages = "com.**.db1.mapper", sqlSessionFactoryRef = "sqlSessionFactoryDb1")
@Configuration
public class DataSourceConfigDB1 {

    @Value("${spring.datasource.db1.jdbcUrl}")
    private String url;

    @Bean(name = "dataSourceDb1")
    @ConfigurationProperties(prefix = "spring.datasource.db1")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sqlSessionFactoryDb1")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSourceDb1") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();

        //添加PageHelper插件
        Interceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("reasonable", "false");
        interceptor.setProperties(properties);
        bean.setPlugins(new Interceptor[]{interceptor,new CatMybatisInterceptor(url)});
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "sqlSessionTemplateDb1")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("sqlSessionFactoryDb1") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "dataSourceTransactionManagerDb1")
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSourceDb1") DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }



}
