package com.eda_project.tickets.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.events}")
    private String eventsExchangeName;

    @Value("${rabbitmq.exchange.tickets}")
    private String ticketsExchangeName;

    @Value("${rabbitmq.exchange.payments}")
    private String paymentsExchangeName;

    @Value("${rabbitmq.queue.event-created}")
    private String eventCreatedQueueName;

    @Value("${rabbitmq.queue.payment-confirmed}")
    private String paymentConfirmedQueueName;

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchangeName);
    }

    @Bean
    public DirectExchange ticketsExchange() {
        return new DirectExchange(ticketsExchangeName);
    }

    @Bean
    public FanoutExchange paymentsExchange() {
        return new FanoutExchange(paymentsExchangeName);
    }

    @Bean
    public Queue eventCreatedQueue() {
        return new Queue(eventCreatedQueueName, true);
    }

    @Bean
    public Binding eventCreatedBinding(
            Queue eventCreatedQueue,
            TopicExchange eventsExchange) {
        return BindingBuilder.bind(eventCreatedQueue)
                .to(eventsExchange)
                .with("event.created");
    }

    @Bean
    public Queue paymentConfirmedQueue() {
        return new Queue(paymentConfirmedQueueName, true);
    }

    @Bean
    public Binding paymentConfirmedBinding(
            Queue paymentConfirmedQueue,
            FanoutExchange paymentsExchange) {
        return BindingBuilder.bind(paymentConfirmedQueue)
                .to(paymentsExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
