package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({FilmController.class, UserController.class})
public class ValidationModelsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilmService filmService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm;
    private Film invalidFilm;

    private User validUser;
    private User invalidUser;

    @BeforeEach
    void setup() {
        validFilm = createValidFilm();
        invalidFilm = createInvalidFilm();

        validUser = createValidUser();
        invalidUser = createInvalidUser();
    }

    @Test
    void addValidFilm_ShouldReturnOk() throws Exception {
        when(filmService.addFilm(validFilm)).thenReturn(validFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(validFilm.getName()));
    }

    @Test
    void addInvalidFilm_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_NonExistent_ShouldReturn404() throws Exception {
        Film filmToUpdate = new Film();
        filmToUpdate.setId(999);
        filmToUpdate.setName("Обновленный фильм");
        filmToUpdate.setDescription("Обновление");
        filmToUpdate.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmToUpdate.setDuration(100);

        when(filmService.updateFilm(filmToUpdate)).thenThrow(new ValidationException("Фильм не найден"));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createValidUser_ShouldReturnOk() throws Exception {
        when(userService.addUser(validUser)).thenReturn(validUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(validUser.getEmail()));
    }

    @Test
    void createInvalidUser_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.login").exists())
                .andExpect(jsonPath("$.birthday").exists());
    }

    @Test
    void updateUser_NonExistent_ShouldReturnError() throws Exception {
        User nonExistentUser = new User();
        nonExistentUser.setId(999);
        nonExistentUser.setEmail("test@test.com");
        nonExistentUser.setLogin("login");
        nonExistentUser.setName("Name");
        nonExistentUser.setBirthday(LocalDate.of(1946, 1, 1));

        when(userService.updateUser(nonExistentUser)).thenThrow(new ValidationException("Пользователь не найден"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createFilmWithInvalidReleaseDate() throws Exception {
        String json = "{ \"name\": \"Test Film\", \"description\": \"Test description\", \"releaseDate\": \"1800-01-01\", \"duration\": 120 }";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.releaseDate").value("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setId(1);
        film.setName("Тестовый фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    private Film createInvalidFilm() {
        Film film = new Film();
        film.setId(2);
        film.setName("");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(3000, 1, 1));
        return film;
    }

    private User createValidUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 5, 20));
        return user;
    }

    private User createInvalidUser() {
        User user = new User();
        user.setId(2);
        user.setEmail("invalid-email");
        user.setLogin("invalid login");
        user.setName("");
        user.setBirthday(LocalDate.of(3000, 1, 1));
        return user;
    }

}
