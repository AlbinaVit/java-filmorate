package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    private final UserService userService;

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(long filmId, long userId) {
        Film film = getFilmById(filmId);
        if (film == null) {
            throw new ValidationException("Фильм не найден");
        }
        try {
            userService.getUserById(userId);
        } catch (ValidationException e) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден");
        }

        if (!film.getLikedUsers().add(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }
    }

    public void removeLike(long filmId, long userId) {
        Film film = getFilmById(filmId);
        if (film == null) {
            throw new ValidationException("Фильм не найден");
        }
        try {
            userService.getUserById(userId);
        } catch (ValidationException e) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден");
        }

        if (!film.getLikedUsers().remove(userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingLong((Film film) -> film.getLikedUsers().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

}
