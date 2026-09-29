package com.eda_project.notifications.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String EVENTS_TOPIC_EXCHANGE = "events.topic";
    public static final String PAYMENTS_FANOUT_EXCHANGE = "payments.fanout";

    // Filas consumidas por Notifications
    public static final String QUEUE_NOTIF_EVENTS = "notifications.events.queue";
    public static final String QUEUE_NOTIF_PAYMENT = "notif.payment-confirmed.queue";
    public static final String QUEUE_NOTIF_CHECKIN = "notif.checkin.queue";
    public static final String QUEUE_NOTIF_SOLDOUT = "notif.soldout.queue";

    // Routing Keys (Topic)
    public static final String ROUTING_KEY_EVENT_CREATED = "event.created";
    public static final String ROUTING_KEY_TICKET_CHECKED = "ticket.checked";
    public static final String ROUTING_KEY_EVENT_SOLDOUT = "event.soldout";

    @Bean
    public TopicExchange eventsTopicExchange() {
        return new TopicExchange(EVENTS_TOPIC_EXCHANGE);
    }

    @Bean
    public FanoutExchange paymentsFanoutExchange() {
        return new FanoutExchange(PAYMENTS_FANOUT_EXCHANGE);
    }

    // Declaração das Filas
    @Bean
    public Queue notifEventsQueue() {
        return QueueBuilder.durable(QUEUE_NOTIF_EVENTS).build();
    }

    @Bean
    public Queue notifPaymentQueue() {
        return QueueBuilder.durable(QUEUE_NOTIF_PAYMENT).build();
    }

    @Bean
    public Queue notifCheckinQueue() {
        return QueueBuilder.durable(QUEUE_NOTIF_CHECKIN).build();
    }

    @Bean
    public Queue notifSoldoutQueue() {
        return QueueBuilder.durable(QUEUE_NOTIF_SOLDOUT).build();
    }

    // Bindings
    @Bean
    public Binding bindingNotifEvents(Queue notifEventsQueue, TopicExchange eventsTopicExchange) {
        return BindingBuilder.bind(notifEventsQueue).to(eventsTopicExchange).with(ROUTING_KEY_EVENT_CREATED);
    }

    @Bean
    public Binding bindingNotifPayment(Queue notifPaymentQueue, FanoutExchange paymentsFanoutExchange) {
        return BindingBuilder.bind(notifPaymentQueue).to(paymentsFanoutExchange);
    }

    @Bean
    public Binding bindingNotifCheckin(Queue notifCheckinQueue, TopicExchange eventsTopicExchange) {
        return BindingBuilder.bind(notifCheckinQueue).to(eventsTopicExchange).with(ROUTING_KEY_TICKET_CHECKED);
    }

    @Bean
    public Binding bindingNotifSoldout(Queue notifSoldoutQueue, TopicExchange eventsTopicExchange) {
        return BindingBuilder.bind(notifSoldoutQueue).to(eventsTopicExchange).with(ROUTING_KEY_EVENT_SOLDOUT);
    }

    // Conversor JSON e RabbitTemplate
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}