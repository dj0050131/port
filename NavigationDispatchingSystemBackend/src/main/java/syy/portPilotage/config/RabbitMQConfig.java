package syy.portPilotage.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import syy.portPilotage.constant.RabbitMQConstant;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        return factory;
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(RabbitMQConstant.EXCHANGE_NOTIFICATION);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(RabbitMQConstant.QUEUE_SMS);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(RabbitMQConstant.QUEUE_EMAIL);
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue()).to(notificationExchange()).with(RabbitMQConstant.ROUTING_KEY_SMS);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(notificationExchange()).with(RabbitMQConstant.ROUTING_KEY_EMAIL);
    }

    @Bean
    public DirectExchange pilotageExchange() {
        return new DirectExchange(RabbitMQConstant.EXCHANGE_PILOTAGE);
    }

    @Bean
    public Queue planNotifyQueue() {
        return new Queue(RabbitMQConstant.QUEUE_PLAN_NOTIFY);
    }

    @Bean
    public Queue certificateReminderQueue() {
        return new Queue(RabbitMQConstant.QUEUE_CERTIFICATE_REMINDER);
    }

    @Bean
    public Binding planNotifyBinding() {
        return BindingBuilder.bind(planNotifyQueue()).to(pilotageExchange()).with(RabbitMQConstant.ROUTING_KEY_PLAN_NOTIFY);
    }

    @Bean
    public Binding certificateReminderBinding() {
        return BindingBuilder.bind(certificateReminderQueue()).to(pilotageExchange()).with(RabbitMQConstant.ROUTING_KEY_CERTIFICATE);
    }

    @Bean
    public DirectExchange billingExchange() {
        return new DirectExchange(RabbitMQConstant.EXCHANGE_BILLING);
    }

    @Bean
    public Queue billingCalculateQueue() {
        return new Queue(RabbitMQConstant.QUEUE_BILLING_CALCULATE);
    }

    @Bean
    public Binding billingCalculateBinding() {
        return BindingBuilder.bind(billingCalculateQueue()).to(billingExchange()).with(RabbitMQConstant.ROUTING_KEY_BILLING);
    }
}