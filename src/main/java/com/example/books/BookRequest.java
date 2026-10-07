package com.example.books;

import jakarta.validation.constraints.NotBlank;

public record BookRequest(@NotBlank String title, @NotBlank String author) {
}
