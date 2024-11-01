package com.tlc.test.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
@MapperScan(
        sqlSessionFactoryRef = "subscribeSqlSessionFactory"
)
public class SubscribeConfig {
    @Bean(name = "subscribeDataSource")
    @ConfigurationProperties(prefix = "spring.subscribe-datasource")
    public DataSource subscribeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "subscribeSqlSessionFactory")
    public SqlSessionFactory subscribeSqlSessionFactory(@Qualifier("subscribeDataSource") DataSource dataSource,
                                                         ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        //sqlSessionFactoryBean.setTypeAliasesPackage("com.example.batchtasklet.vo");
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:mappers/subscribe/**.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "subscribeSessionTemplate")
    public SqlSessionTemplate subscribeSqlSessionTemplate(@Qualifier("subscribeSqlSessionFactory") SqlSessionFactory subscribeSqlSessionFactory) {
        return new SqlSessionTemplate(subscribeSqlSessionFactory);
    }

    @Bean(name = "subscribeDataSourceInitializer")
    public DataSourceInitializer dataSourceInitializer2(@Qualifier("subscribeDataSource") DataSource datasource) {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        //resourceDatabasePopulator.addScript(new ClassPathResource("schema-subscribe.sql"));

        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(datasource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
        return dataSourceInitializer;
    }
}
