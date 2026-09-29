package com.eda_project.events.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
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
    private String exchangeName;

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public FanoutExchange paymentsExchange() {
        return new FanoutExchange("payments.fanout");
    }

    @Bean
    public Queue paymentConfirmedEventsQueue() {
        return new Queue("events.payment-confirmed.queue", true);
    }

    @Bean
    public Binding paymentConfirmedEventsBinding(
            Queue paymentConfirmedEventsQueue,
            FanoutExchange paymentsExchange) {
        return BindingBuilder.bind(paymentConfirmedEventsQueue)
                .to(paymentsExchange);
    }

    @Bean
    public Queue ticketCheckedEventsQueue() {
        return new Queue("events.checkin.queue", true);
    }

    @Bean
    public Binding ticketCheckedEventsBinding(
            Queue ticketCheckedEventsQueue,
            TopicExchange eventsExchange) {
        return BindingBuilder.bind(ticketCheckedEventsQueue)
                .to(eventsExchange)
                .with("ticket.checked");
    }

    @Bean
    public Queue eventSoldOutQueue() {
        return new Queue("events.soldout.queue", true);
    }

    @Bean
    public Binding eventSoldOutBinding(
            Queue eventSoldOutQueue,
            TopicExchange eventsExchange) {
        return BindingBuilder.bind(eventSoldOutQueue)
                .to(eventsExchange)
                .with("event.soldout");
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
