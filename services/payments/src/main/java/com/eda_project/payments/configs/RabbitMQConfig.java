package com.eda_project.payments.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nomes de Exchanges
    public static final String TICKETS_DIRECT = "tickets.direct";
    public static final String PAYMENTS_FANOUT = "payments.fanout";

    // Fila e Chave de Consumo
    public static final String ROUTING_KEY_TICKET_PURCHASED = "ticket.purchased";
    public static final String QUEUE_PAYMENT_PROCESS = "payment.process.queue";

    // Filas de Distribuição Fanout
    public static final String QUEUE_TICKETS_PAID = "tickets.payment-confirmed.queue";
    public static final String QUEUE_EVENTS_PAID = "events.payment-confirmed.queue";
    public static final String QUEUE_NOTIF_PAID = "notif.payment-confirmed.queue";

    @Bean
    public DirectExchange ticketsDirectExchange() {
        return new DirectExchange(TICKETS_DIRECT);
    }

    @Bean
    public FanoutExchange paymentsFanoutExchange() {
        return new FanoutExchange(PAYMENTS_FANOUT);
    }

    // Fila do processador de pagamento
    @Bean
    public Queue paymentProcessQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_PROCESS).build();
    }

    @Bean
    public Binding bindingPaymentProcess(Queue paymentProcessQueue, DirectExchange ticketsDirectExchange) {
        return BindingBuilder.bind(paymentProcessQueue).to(ticketsDirectExchange).with(ROUTING_KEY_TICKET_PURCHASED);
    }

    // Declaração e binding das filas receptoras no Fanout
    @Bean
    public Queue ticketsPaidQueue() {
        return QueueBuilder.durable(QUEUE_TICKETS_PAID).build();
    }

    @Bean
    public Queue eventsPaidQueue() {
        return QueueBuilder.durable(QUEUE_EVENTS_PAID).build();
    }

    @Bean
    public Queue notifPaidQueue() {
        return QueueBuilder.durable(QUEUE_NOTIF_PAID).build();
    }

    @Bean
    public Binding bindTicketsPaid(Queue ticketsPaidQueue, FanoutExchange paymentsFanoutExchange) {
        return BindingBuilder.bind(ticketsPaidQueue).to(paymentsFanoutExchange);
    }

    @Bean
    public Binding bindEventsPaid(Queue eventsPaidQueue, FanoutExchange paymentsFanoutExchange) {
        return BindingBuilder.bind(eventsPaidQueue).to(paymentsFanoutExchange);
    }

    @Bean
    public Binding bindNotifPaid(Queue notifPaidQueue, FanoutExchange paymentsFanoutExchange) {
        return BindingBuilder.bind(notifPaidQueue).to(paymentsFanoutExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}