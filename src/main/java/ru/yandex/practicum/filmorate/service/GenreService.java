package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDao genreDao;

    public List<Genre> getAllGenres() {
        return genreDao.getAllGenres();
    }

    public Genre getGenreById(int id) {
        return genreDao.getGenreById(id);
    }

    public List<Genre> getGenresByFilmId(long filmId) {
        return genreDao.getGenresByFilmId(filmId);
    }

    public Map<Long, List<Genre>> getAllGenresByFilms() {
        return genreDao.getAllGenresByFilms();
    }
}

