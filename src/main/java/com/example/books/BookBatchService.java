package com.example.books;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class BookBatchService {
    private static final Logger log = LoggerFactory.getLogger(BookBatchService.class);

    private final BookRepository repository;
    private final BookEventPublisher eventPublisher;

    public BookBatchService(BookRepository repository, BookEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public List<Book> updateBatch(BatchBookUpdateRequest request) {
        List<Book> updated = new ArrayList<>();
        for (BatchBookUpdateRequest.Item item : request.updates()) {
            Book book = repository.findById(item.id())
                    .orElseThrow(() -> new BookNotFoundException(item.id()));
            book.update(item.title(), item.author());
            updated.add(repository.save(book));
        }

        List<Book> committedBooks = List.copyOf(updated);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                committedBooks.forEach(book -> {
                    try {
                        eventPublisher.publish("UPDATED", book);
                    } catch (RuntimeException error) {
                        log.error("Could not start publishing update event for book {}", book.getId(), error);
                    }
                });
            }
        });
        return updated;
    }
}
