package com.tlc.test.job;

import com.tlc.test.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class SimpleBatchJob {
    @Value("${chunkSize:100}")
    private int chunkSize;

    @Bean
    public Job simpleJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("runBalanceJob", jobRepository)
            .start(step1)
            .build();
    }

    @Bean
    public Step myStep(JobRepository jobRepository, @Qualifier("chainTxManager") PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
            .<Account, Account>chunk(chunkSize, transactionManager)
            .faultTolerant()
            .reader(reader(null))
            .processor(bProcessor())
            .writer(aWriter())
            .transactionAttribute(txAttribute())
            .build();
    }

    @Bean
    CompositeItemWriter<Account> aWriter() {
        return new CompositeItemWriterBuilder<Account>()
            .delegates(write(null), update(null))
            .build();
    }

    @Bean
    @StepScope
    public MyBatisPagingItemReader<Account> reader(@Qualifier("publishSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new MyBatisPagingItemReaderBuilder<Account>()
            .sqlSessionFactory(sqlSessionFactory)
            .queryId("publish.selectAll")
            .build();
    }

    @Bean
    @StepScope
    ItemProcessor<Account, Account> bProcessor() {
        return aUser -> Account.builder()
            .id(aUser.getId())
            .content(aUser.getContent())
            .post("Y")
            .build();
    }

    @Bean
    @StepScope
    public MyBatisBatchItemWriter<Account> write(@Qualifier("subscribeSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new MyBatisBatchItemWriterBuilder<Account>()
            .sqlSessionFactory(sqlSessionFactory)
            .statementId("subscribe.insert")
            .build();
    }

    @Bean
    @StepScope
    public MyBatisBatchItemWriter<Account> update(@Qualifier("publishSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new MyBatisBatchItemWriterBuilder<Account>()
            .sqlSessionFactory(sqlSessionFactory)
            .assertUpdates(false)
            .statementId("publish.update")
            .build();
    }

    DefaultTransactionAttribute txAttribute() {
        DefaultTransactionAttribute txAttribute = new DefaultTransactionAttribute();
        txAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txAttribute.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        txAttribute.setTimeout(600);
        return txAttribute;
    }
}