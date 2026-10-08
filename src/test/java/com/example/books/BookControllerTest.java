package com.example.books;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false"
})
@AutoConfigureMockMvc
class BookControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookRepository repository;

    @MockitoBean
    private BookEventPublisher eventPublisher;

    @BeforeEach
    void clearBooks() {
        repository.deleteAll();
    }

    @Test
    void supportsCrud() throws Exception {
        String location = mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","author":"Robert C. Martin"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Robert C. Martin"));

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"The Pragmatic Programmer","author":"Andy Hunt"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Pragmatic Programmer"));

        mvc.perform(delete(location))
                .andExpect(status().isNoContent());

        mvc.perform(get(location))
                .andExpect(status().isNotFound());

        verify(eventPublisher).publish(eq("CREATED"), any(Book.class));
        verify(eventPublisher).publish(eq("UPDATED"), any(Book.class));
        verify(eventPublisher).publish(eq("DELETED"), any(Book.class));
    }

    @Test
    void rejectsInvalidRequestsAndMissingBooks() throws Exception {
        mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","author":"Someone"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid book request"))
                .andExpect(jsonPath("$.errors.title").value("Title is required"));

        mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "x".repeat(256) + "\",\"author\":\"Someone\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Title must be 255 characters or fewer"));

        mvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.author").value("Author is required"));

        mvc.perform(get("/api/books/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").value("Book ID must be positive"));

        mvc.perform(get("/api/books/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").value("Book ID must be a number"));

        mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid JSON request body"));

        mvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book 999 not found"));

        mvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book 999 not found"));
    }

    @Test
    void batchUpdateCommitsTogetherAndRollsBackOnMissingBook() throws Exception {
        Book first = repository.save(new Book("First edition", "Author One"));
        Book second = repository.save(new Book("Second edition", "Author Two"));

        mvc.perform(put("/api/books/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"updates":[
                                  {"id":%d,"title":"Changed first","author":"Author One"},
                                  {"id":999999,"title":"Missing","author":"Nobody"}
                                ]}
                                """.formatted(first.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book 999999 not found"));

        assertEquals("First edition",
                repository.findById(first.getId()).orElseThrow().getTitle());
        verify(eventPublisher, never()).publish(eq("UPDATED"), any(Book.class));

        mvc.perform(put("/api/books/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"updates":[
                                  {"id":%d,"title":"Changed first","author":"Author One"},
                                  {"id":%d,"title":"Changed second","author":"Author Two"}
                                ]}
                                """.formatted(first.getId(), second.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Changed first"))
                .andExpect(jsonPath("$[1].title").value("Changed second"));

        assertEquals("Changed first",
                repository.findById(first.getId()).orElseThrow().getTitle());
        assertEquals("Changed second",
                repository.findById(second.getId()).orElseThrow().getTitle());
        verify(eventPublisher, times(2)).publish(eq("UPDATED"), any(Book.class));
    }

    @Test
    void rejectsEmptyBatch() throws Exception {
        mvc.perform(put("/api/books/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"updates\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.updates").value("At least one book is required"));
    }
}
