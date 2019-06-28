package org.absit.integrasjon.config;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.spring.spi.TransactionErrorHandlerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ActivemqConfiguration {

    @Value("${amq.concurrent-consumers}")
    private int amqConcurrentConsumers;

    @Value("${amq.broker-url}")
    private String brokerUrl;

    @Value("${amq.user}")
    private String brokerUser;

    @Value("${amq.password}")
    private String brokerPassword;

    @Value("${amq.pool.max-connections}")
    private int poolMaxConnections;

    @Value("${amq.pool.maximum-active-session-per-connection}")
    private int poolMaxActiveSessionsPerConnection;

    @Value("${amq.pool.block-if-full}")
    private boolean poolBlockIfFull;

    @Value("${amq.pool.block-if-full-timeout}")
    private int poolBlockIfFullTimeout;

    @Value("${amq.maximumRedeliveries}")
    private int maximumRedeliveries;

    @Value("${amq.redeliveryDelay}")
    private int redeliveryDelay;

    @Value("${amq.backOffMultiplier}")
    private int backOffMultiplier;

    @Value("${amq.useExponentialBackOff}")
    private boolean useExponentialBackOff;

    @Value("${amq.maximumRedeliveryDelay}")
    private int maximumRedeliveryDelay;

    @Bean
    @Inject
    public PlatformTransactionManager jmsTransactionManager(PooledConnectionFactory pooledConnectionFactory) {
        return new JmsTransactionManager(pooledConnectionFactory);
    }

    @Bean
    @Inject
    public TransactionErrorHandlerBuilder transactionErrorHandlerBuilder(
        PlatformTransactionManager jmsTransactionManager) {
        TransactionErrorHandlerBuilder transactionErrorHandlerBuilder = new TransactionErrorHandlerBuilder();
        transactionErrorHandlerBuilder.setTransactionManager(jmsTransactionManager);
        return transactionErrorHandlerBuilder;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    PooledConnectionFactory pooledConnectionFactory(ActiveMQConnectionFactory activeMQConnectionFactory) {
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setMaxConnections(poolMaxConnections);
        pooledConnectionFactory.setMaximumActiveSessionPerConnection(poolMaxActiveSessionsPerConnection);
        pooledConnectionFactory.setBlockIfSessionPoolIsFull(poolBlockIfFull);
        pooledConnectionFactory.setBlockIfSessionPoolIsFullTimeout(poolBlockIfFullTimeout);
        return pooledConnectionFactory;
    }

    @Bean
    public RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(maximumRedeliveries);
        redeliveryPolicy.setRedeliveryDelay(redeliveryDelay);
        redeliveryPolicy.setBackOffMultiplier(backOffMultiplier);
        redeliveryPolicy.setUseExponentialBackOff(useExponentialBackOff);
        redeliveryPolicy.setMaximumRedeliveryDelay(maximumRedeliveryDelay);
        return redeliveryPolicy;
    }

    @Bean
    @Primary
    @Inject
    public ActiveMQConnectionFactory activeMQConnectionFactory(RedeliveryPolicy redeliveryPolicy) {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        activeMQConnectionFactory.setPassword(brokerPassword);
        activeMQConnectionFactory.setUserName(brokerUser);
        activeMQConnectionFactory.setNonBlockingRedelivery(true);
        activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        return activeMQConnectionFactory;
    }

    @Bean
    @Inject
    public JmsConfiguration jmsConfiguration(@Named("pooledConnectionFactory") PooledConnectionFactory pooledConnectionFactory,
        @Named("activeMQConnectionFactory") ActiveMQConnectionFactory activeMQConnectionFactory) {
        JmsConfiguration jmsConfig = new JmsConfiguration();
        jmsConfig.setErrorHandlerLogStackTrace(true);
        jmsConfig.setTransacted(true);
        jmsConfig.setErrorHandlerLoggingLevel(LoggingLevel.INFO);
        jmsConfig.setConcurrentConsumers(amqConcurrentConsumers);
        jmsConfig.setListenerConnectionFactory(activeMQConnectionFactory);
        jmsConfig.setTemplateConnectionFactory(pooledConnectionFactory);
        return jmsConfig;
    }

    @Bean
    @Inject
    public ActiveMQComponent activemq(PooledConnectionFactory pooledConnectionFactory,JmsConfiguration jmsConfiguration,
        PlatformTransactionManager jmsTransactionManager) {
        ActiveMQComponent aqComponent = new ActiveMQComponent();
        aqComponent.setConfiguration(jmsConfiguration);
        aqComponent.setTransactionManager(jmsTransactionManager);
        aqComponent.setCacheLevelName("CACHE_CONSUMER");
        return aqComponent;
    }
}
