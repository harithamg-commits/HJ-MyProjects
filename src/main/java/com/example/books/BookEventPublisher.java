package com.example.books;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(BookEventPublisher.class);
    private static final String TOPIC = "book-events";

    private final KafkaTemplate<String, BookEvent> kafkaTemplate;

    public BookEventPublisher(KafkaTemplate<String, BookEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String action, Book book) {
        BookEvent event = new BookEvent(action, book.getId(), book.getTitle(), book.getAuthor());
        kafkaTemplate.send(TOPIC, book.getId().toString(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Could not publish book event {} for book {}", action, book.getId(), error);
                    }
                });
    }
}
