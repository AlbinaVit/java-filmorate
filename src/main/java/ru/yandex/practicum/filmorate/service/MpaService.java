package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDao mpaDao;

    public List<MpaRating> getAllMpa() {
        return mpaDao.getAllMpa();
    }

    public MpaRating getMpaById(int id) {
        return mpaDao.getMpaById(id);
    }


}
