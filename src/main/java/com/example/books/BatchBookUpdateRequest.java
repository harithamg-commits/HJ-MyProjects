package com.example.books;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BatchBookUpdateRequest(
        @NotEmpty(message = "At least one book is required") List<@NotNull @Valid Item> updates) {

    public record Item(
            @NotNull(message = "Book ID is required") @Positive(message = "Book ID must be positive") Long id,
            @NotBlank(message = "Title is required") @Size(max = 255, message = "Title must be 255 characters or fewer") String title,
            @NotBlank(message = "Author is required") @Size(max = 255, message = "Author must be 255 characters or fewer") String author) {
    }
}
