package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> createPreparedStatement(connection, sql, film), keyHolder);

        long id = keyHolder.getKey().longValue();
        film.setId(id);
        saveFilmGenres(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "WHERE id = ?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        deleteFilmGenres(film.getId());
        saveFilmGenres(film);

        return film;
    }

    @Override
    public Film getFilmById(long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setLikedUsers(getLikesByFilmId(film.getId()));
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setMpa(new MpaRating(rs.getInt("mpa_rating_id"), null));
            return film;
        }, id);

        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return films.get(0);
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new MpaRating(rs.getInt("mpa_id"), rs.getString("mpa_name")));
            film.setLikedUsers(getLikesByFilmId(film.getId()));
            return film;
        });
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, COUNT(fl.user_id) AS likes " +
                "FROM films f " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "GROUP BY f.id, m.id, m.name " +
                "ORDER BY likes DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new MpaRating(rs.getInt("mpa_id"), rs.getString("mpa_name")));
            film.setLikedUsers(getLikesByFilmId(film.getId()));
            return film;
        }, count);
    }

    @Override
    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private PreparedStatement createPreparedStatement(Connection connection, String sql, Film film) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, film.getName());
        ps.setString(2, film.getDescription());
        ps.setDate(3, Date.valueOf(film.getReleaseDate()));
        ps.setInt(4, film.getDuration());
        ps.setInt(5, film.getMpa().getId());
        return ps;
    }

    private Set<Long> getLikesByFilmId(long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Long> userIds = jdbcTemplate.queryForList(sql, Long.class, filmId);
        return new HashSet<>(userIds);
    }

    private List<Genre> getGenresByFilmId(long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ), filmId);
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null) return;

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        Set<Integer> uniqueGenreIds = new HashSet<>();
        for (Genre genre : film.getGenres()) {
            if (uniqueGenreIds.add(genre.getId())) {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            }
        }
    }

    private void deleteFilmGenres(long filmId) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
    }

}