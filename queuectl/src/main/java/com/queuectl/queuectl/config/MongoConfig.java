package com.queuectl.queuectl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Collections;

@Configuration
public class MongoConfig {


    @EventListener(ApplicationReadyEvent.class)
    public void configureMongoConverter(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        MappingMongoConverter converter = context.getBean(MappingMongoConverter.class);

        converter.setCustomConversions(new MongoCustomConversions(Collections.emptyList()));
        converter.afterPropertiesSet();

        System.out.println("MongoDB converter configured — IST string timestamps active");
    }
}
