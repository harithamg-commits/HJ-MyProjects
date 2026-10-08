package com.example.books;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank(message = "Title is required") @Size(max = 255, message = "Title must be 255 characters or fewer") String title,
        @NotBlank(message = "Author is required") @Size(max = 255, message = "Author must be 255 characters or fewer") String author) {
}
