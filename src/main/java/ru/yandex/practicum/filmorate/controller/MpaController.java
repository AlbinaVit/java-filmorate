package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public List<MpaRating> getAllMpa() {
        List<MpaRating> mpaRatings = mpaService.getAllMpa();
        log.info("Получен запрос на список всех MPA рейтингов, найдено: {}", mpaRatings.size());
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable int id) {
        MpaRating mpaRating = mpaService.getMpaById(id);
        log.info("Найден MPA рейтинг: {}", mpaRating);
        return mpaService.getMpaById(id);
    }
}
