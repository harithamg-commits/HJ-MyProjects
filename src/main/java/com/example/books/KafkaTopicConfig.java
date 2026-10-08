package com.example.books;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    NewTopic bookEventsTopic() {
        return new NewTopic("book-events", 1, (short) 1);
    }
}
