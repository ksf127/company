package com.company.starter.mq;

import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 只验证自动配置的装配结果，不连接真实 Broker：ApplicationContextRunner 不会发起网络连接。
 */
class CompanyMqAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RabbitAutoConfiguration.class, CompanyMqAutoConfiguration.class));

    @Test
    void shouldRegisterJsonConverterAndPublisherConfirm() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(Jackson2JsonMessageConverter.class);

            RabbitTemplate template = context.getBean(RabbitTemplate.class);
            assertThat(template.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);

            CachingConnectionFactory connectionFactory = context.getBean(CachingConnectionFactory.class);
            assertThat(connectionFactory.isPublisherConfirms()).isTrue();
            assertThat(connectionFactory.isPublisherReturns()).isTrue();
        });
    }

    @Test
    void shouldConfigureListenerRetryAndJsonConverter() {
        runner.run(context -> {
            SimpleRabbitListenerContainerFactory factory =
                    context.getBean("rabbitListenerContainerFactory", SimpleRabbitListenerContainerFactory.class);

            SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
            endpoint.setId("company-mq-test");
            endpoint.setQueueNames("company.mq.test");
            endpoint.setMessageListener(message -> { });

            SimpleMessageListenerContainer container = factory.createListenerContainer(endpoint);

            // messageConverter 由工厂持有，创建容器时逐字段下传
            assertThat(ReflectionTestUtils.getField(factory, "messageConverter"))
                    .isInstanceOf(Jackson2JsonMessageConverter.class);
            assertThat(container.getAcknowledgeMode()).isEqualTo(AcknowledgeMode.AUTO);

            Advice[] adviceChain = (Advice[]) ReflectionTestUtils.getField(container, "adviceChain");
            assertThat(adviceChain).isNotEmpty();
        });
    }
}
