package com.example.books;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("ai")
@RequestMapping("/api/books/ai")
public class BookAgentController {
    private final ChatClient chatClient;

    public BookAgentController(ChatClient.Builder builder, BookAgentTools tools) {
        this.chatClient = builder
                .defaultSystem("You help users explore this library. Use the book tools for library facts. "
                        + "Only recommend books returned by the tools. If the library has no suitable book, say so.")
                .defaultTools(tools)
                .build();
    }

    @PostMapping("/ask")
    public Answer ask(@Valid @RequestBody Question request) {
        String response = chatClient.prompt()
                .user(request.question())
                .call()
                .content();
        return new Answer(response);
    }

    public record Question(@NotBlank String question) {
    }

    public record Answer(String answer) {
    }
}
