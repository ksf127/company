package com.company.starter.mq;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 统一 MQ 行为：JSON 序列化、publisher confirms、消费失败重试。
 * 业务服务只需引入 company-mq-starter，不需要自己声明连接工厂或转换器。
 *
 * <p>必须排在 RabbitAutoConfiguration 之前：Boot 自带的 MessageConverter / 监听容器工厂
 * 都是 @ConditionalOnMissingBean，先注册本类的实现才能让它们正确退让。</p>
 */
@AutoConfiguration(before = RabbitAutoConfiguration.class)
@ConditionalOnClass({RabbitTemplate.class, SimpleRabbitListenerContainerFactory.class})
public class CompanyMqAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CompanyMqAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter jackson2JsonMessageConverter(ObjectProvider<ObjectMapper> objectMapper) {
        ObjectMapper mapper = objectMapper.getIfUnique();
        return mapper == null ? new Jackson2JsonMessageConverter() : new Jackson2JsonMessageConverter(mapper);
    }

    /**
     * 生产者确认：Boot 的 ConnectionFactoryCustomizer 作用于原生
     * {@code com.rabbitmq.client.ConnectionFactory}，拿不到 Spring 的 CachingConnectionFactory，
     * 因此在连接工厂初始化后补齐 correlated confirm 与 return 语义。
     */
    @Bean
    @ConditionalOnMissingBean(name = "publisherConfirmConnectionFactoryPostProcessor")
    public static BeanPostProcessor publisherConfirmConnectionFactoryPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof CachingConnectionFactory caching) {
                    caching.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
                    caching.setPublisherReturns(true);
                }
                return bean;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplateCustomizer publisherConfirmRabbitTemplateCustomizer() {
        return template -> {
            template.setMandatory(true);
            template.setConfirmCallback((correlationData, ack, cause) -> {
                if (!ack) {
                    log.warn("MQ 消息未被 Broker 确认: correlation={}, cause={}",
                            correlationData == null ? null : correlationData.getId(), cause);
                }
            });
            template.setReturnsCallback(returned -> log.warn(
                    "MQ 消息无法路由: exchange={}, routingKey={}, replyText={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));
        };
    }

    /**
     * 消费者：最多重试 3 次（1s 起步、指数退避），仍失败则丢弃且不重回队列，
     * 避免坏消息无限循环拖垮整个服务的消费线程。
     */
    @Bean(name = "rabbitListenerContainerFactory")
    @ConditionalOnMissingBean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            CachingConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
