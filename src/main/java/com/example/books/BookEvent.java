package com.example.books;

public record BookEvent(String action, Long id, String title, String author) {
}
