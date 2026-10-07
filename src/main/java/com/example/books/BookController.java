package com.example.books;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Book> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Book get(@PathVariable Long id) {
        return findBook(id);
    }

    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
        Book book = repository.save(new Book(request.title(), request.author()));
        return ResponseEntity.created(URI.create("/api/books/" + book.getId())).body(book);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        Book book = findBook(id);
        book.update(request.title(), request.author());
        return repository.save(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Book book = findBook(id);
        repository.delete(book);
        return ResponseEntity.noContent().build();
    }

    private Book findBook(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }
}
