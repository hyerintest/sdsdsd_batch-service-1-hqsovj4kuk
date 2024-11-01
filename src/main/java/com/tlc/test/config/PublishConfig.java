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
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
@MapperScan(
        sqlSessionFactoryRef = "publishSqlSessionFactory"
)
public class PublishConfig {
    @Primary
    @Bean(name = "publishDataSource")
    @ConfigurationProperties(prefix = "spring.publish-datasource")
    public DataSource publishDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "publishSqlSessionFactory")
    public SqlSessionFactory publishSqlSessionFactory(@Qualifier("publishDataSource") DataSource firstDataSource,
                                                      ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(firstDataSource);
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:mappers/publish/**.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Primary
    @Bean(name = "publishSessionTemplate")
    public SqlSessionTemplate publishSqlSessionTemplate(@Qualifier("publishSqlSessionFactory") SqlSessionFactory publishSqlSessionFactory) {
        return new SqlSessionTemplate(publishSqlSessionFactory);
    }

    @Bean(name = "publishDataSourceInitializer")
    public DataSourceInitializer dataSourceInitializer(@Qualifier("publishDataSource") DataSource datasource) {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        //resourceDatabasePopulator.addScript(new ClassPathResource("schema-publish.sql"));

        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(datasource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
        return dataSourceInitializer;
    }
}
