package com.example.books;

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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookRepository repository;

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
    }

    @Test
    void rejectsBlankFieldsAndMissingBooks() throws Exception {
        mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","author":"Someone"}
                                """))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound());
    }
}
