package com.tlc.test.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties
public class DataSourceProperties {

    @Bean
    @Primary
    PlatformTransactionManager publishDbTxManager(
            @Qualifier("publishDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    PlatformTransactionManager subscribeDbTxManager(@Qualifier("subscribeDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    PlatformTransactionManager chainTxManager() {
        // 2개 이상의 TransactionManager를 연결할때 사용
        ChainedTransactionManager txManager = new ChainedTransactionManager(publishDbTxManager(null), subscribeDbTxManager(null));
        return txManager;
    }
}