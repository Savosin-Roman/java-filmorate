package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaRowMapper;

    public List<Mpa> findAll() {
        log.debug("Получение всех рейтингов MPA");
        String sql = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
        return jdbc.query(sql, mpaRowMapper);
    }

    public Mpa findById(Long id) {
        log.debug("Поиск рейтинга MPA с ID: {}", id);
        String sql = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
        try {
            return jdbc.queryForObject(sql, mpaRowMapper, id);
        } catch (Exception e) {
            throw new NotFoundException("Рейтинг MPA с ID " + id + " не найден");
        }
    }
}