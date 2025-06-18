package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    private final UserService userService;

    private final MpaDao mpaDao;

    private final GenreDao genreDao;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService, MpaDao mpaDao, GenreDao genreDao) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaDao = mpaDao;
        this.genreDao = genreDao;
    }

    public Film addFilm(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == 0) {
            throw new ValidationException("Необходимо указать рейтинг MPA");
        }
        film.setMpa(mpaDao.getMpaById(film.getMpa().getId()));
        log.info("Добавлено поле мра = {}", film.getMpa());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> genresFromDb = new LinkedHashSet<>();
            for (Genre genre : film.getGenres()) {
                var dbGenre = genresFromDb.add(genreDao.getGenreById(genre.getId()));
                log.debug("Загруженный жанр: {}", dbGenre);
            }
            film.setGenres(new ArrayList<>(genresFromDb));
            log.info("Фильм {} имеет жанры", film.getGenres());
        } else {
            film.setGenres(new ArrayList<>());
            log.info("Фильм {} не имеет жанров", film.getGenres());
        }

        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        film.setMpa(mpaDao.getMpaById(film.getMpa().getId()));

        if (film.getGenres() != null) {
            Set<Genre> genresFromDb = new LinkedHashSet<>();
            film.getGenres().stream()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .forEach(genre -> genresFromDb.add(genreDao.getGenreById(genre.getId())));
            film.setGenres(new ArrayList<>(genresFromDb));
        }

        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(long id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(long filmId, long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId);
        filmStorage.addLike(filmId, userId);

        if (!film.getLikedUsers().add(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }
    }

    public void removeLike(long filmId, long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId);

        if (!film.getLikedUsers().remove(userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

}
