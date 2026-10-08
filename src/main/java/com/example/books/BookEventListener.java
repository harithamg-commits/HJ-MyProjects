package com.example.books;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookEventListener {
    private static final Logger log = LoggerFactory.getLogger(BookEventListener.class);

    @KafkaListener(topics = "book-events")
    public void onBookEvent(BookEvent event) {
        log.info("Received book event: {}", event);
    }
}
