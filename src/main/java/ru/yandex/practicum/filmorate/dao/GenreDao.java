package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class GenreDao {
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }

    public Genre getGenreById(long id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ), id);
        if (genres.isEmpty()) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
        return genres.get(0);
    }

    public Map<Long, List<Genre>> getAllGenresByFilms() {
        String sql = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id";
        List<Map.Entry<Long, Genre>> entries = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            return Map.entry(filmId, genre);
        });
        Map<Long, List<Genre>> map = new HashMap<>();
        for (Map.Entry<Long, Genre> entry : entries) {
            map.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
        }
        return map;
    }

    public List<Genre> getGenresByFilmId(long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ), filmId);
    }
}
