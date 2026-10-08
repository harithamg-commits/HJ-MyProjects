package com.example.books;

import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ai")
public class BookAgentTools {
    private final BookRepository repository;

    public BookAgentTools(BookRepository repository) {
        this.repository = repository;
    }

    @Tool(description = "List the books currently in this library, including each book's ID, title, and author")
    public List<BookInfo> listBooks() {
        return repository.findAll().stream()
                .map(book -> new BookInfo(book.getId(), book.getTitle(), book.getAuthor()))
                .toList();
    }

    @Tool(description = "Look up one book in this library by its ID")
    public String findBook(@ToolParam(description = "The numeric book ID") Long id) {
        return repository.findById(id)
                .map(book -> "ID: %d, title: %s, author: %s"
                        .formatted(book.getId(), book.getTitle(), book.getAuthor()))
                .orElse("No book found with ID " + id);
    }

    public record BookInfo(Long id, String title, String author) {
    }
}
