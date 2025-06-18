package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDao {
    private final JdbcTemplate jdbcTemplate;

    public List<MpaRating> getAllMpa() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";  // Добавляем сортировку по id
        return jdbcTemplate.query(sql, (rs, rowNum) -> new MpaRating(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }

    public MpaRating getMpaById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(sql, (rs, rowNum) -> new MpaRating(
                rs.getInt("id"),
                rs.getString("name")
        ), id);
        if (ratings.isEmpty()) {
            throw new NotFoundException("Рейтинг с id=" + id + " не найден");
        }
        return ratings.get(0);
    }
}
