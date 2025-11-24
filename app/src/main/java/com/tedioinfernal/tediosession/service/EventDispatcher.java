package com.tedioinfernal.tediosession.service;

import com.tedioinfernal.tediosession.annotation.EventHandler;
import com.tedioinfernal.tediosession.dto.MessageDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EventDispatcher {

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, EventHandlerService> eventHandlers = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EventHandler.class);
        
        for (Object bean : beans.values()) {
            if (bean instanceof EventHandlerService) {
                // Pega a classe real, mesmo se for um proxy
                Class<?> targetClass = AopUtils.getTargetClass(bean);
                EventHandler annotation = targetClass.getAnnotation(EventHandler.class);
                
                if (annotation != null) {
                    String eventName = annotation.value();
                    eventHandlers.put(eventName, (EventHandlerService) bean);
                    log.info("Registered event handler for event: {}", eventName);
                }
            }
        }
    }

    public void dispatch(MessageDTO message) {
        String event = message.getEvent();
        log.info("Dispatching event: {}", event);
        
        EventHandlerService handler = eventHandlers.get(event);
        
        if (handler != null) {
            handler.handle(message.getObject());
        } else {
            log.warn("No handler found for event: {}", event);
        }
    }
}
