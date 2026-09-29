package com.eda_project.events.listener;

import com.eda_project.events.event.EventSoldOutEvent;
import com.eda_project.events.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventSoldOutListener {

    private final EventService eventService;

    @RabbitListener(queues = "events.soldout.queue")
    public void handle(EventSoldOutEvent event) {
        log.info("EVENT_SOLD_OUT recebido: eventId={}", event.getEventId());
        eventService.markSoldOut(event.getEventId());
    }
}
