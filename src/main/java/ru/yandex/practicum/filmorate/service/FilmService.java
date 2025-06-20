package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    private final UserService userService;

    private final MpaService mpaService;

    private final GenreService genreService;
    private final GenreDao genreDao;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService, MpaService mpaService, GenreService genreService, GenreDao genreDao) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.genreDao = genreDao;
    }

    public Film addFilm(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == 0) {
            throw new ValidationException("Необходимо указать рейтинг MPA");
        }
        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
        log.info("Добавлено поле мра = {}", film.getMpa());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> genresFromDb = new LinkedHashSet<>();
            for (Genre genre : film.getGenres()) {
                var dbGenre = genresFromDb.add(genreService.getGenreById(genre.getId()));
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
        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));

        if (film.getGenres() != null) {
            Set<Genre> genresFromDb = new LinkedHashSet<>();
            film.getGenres().stream()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .forEach(genre -> genresFromDb.add(genreService.getGenreById(genre.getId())));
            film.setGenres(new ArrayList<>(genresFromDb));
        }

        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(long id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        Map<Integer, MpaRating> mpaMap = mpaService.getAllMpa().stream()
                .collect(Collectors.toMap(MpaRating::getId, Function.identity()));

        film.setMpa(mpaMap.get(film.getMpa().getId()));
        List<Genre> genres = genreService.getGenresByFilmId(film.getId());
        film.setGenres(genres);
        return film;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        Map<Long, List<Genre>> genresByFilmId = genreService.getAllGenresByFilms();
        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Collections.emptyList()));
        }
        return films;
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
        List<Film> films = filmStorage.getPopularFilms(count);
        Map<Long, List<Genre>> genresByFilmId = genreService.getAllGenresByFilms();
        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Collections.emptyList()));
        }
        return films;
    }

    public MpaRating getMpaRatingById(int id) {
        return mpaService.getMpaById(id);
    }

}
