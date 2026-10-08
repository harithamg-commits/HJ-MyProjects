package com.example.books;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookRepository repository;
    private final BookEventPublisher eventPublisher;
    private final BookBatchService batchService;

    public BookController(BookRepository repository, BookEventPublisher eventPublisher, BookBatchService batchService) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.batchService = batchService;
    }

    @GetMapping
    public List<Book> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Book get(@PathVariable @Positive(message = "Book ID must be positive") Long id) {
        return findBook(id);
    }

    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
        Book book = repository.save(new Book(request.title(), request.author()));
        eventPublisher.publish("CREATED", book);
        return ResponseEntity.created(URI.create("/api/books/" + book.getId())).body(book);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable @Positive(message = "Book ID must be positive") Long id,
            @Valid @RequestBody BookRequest request) {
        Book book = findBook(id);
        book.update(request.title(), request.author());
        Book saved = repository.save(book);
        eventPublisher.publish("UPDATED", saved);
        return saved;
    }

    @PutMapping("/batch")
    public List<Book> updateBatch(@Valid @RequestBody BatchBookUpdateRequest request) {
        return batchService.updateBatch(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Book ID must be positive") Long id) {
        Book book = findBook(id);
        repository.delete(book);
        eventPublisher.publish("DELETED", book);
        return ResponseEntity.noContent().build();
    }

    private Book findBook(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}
