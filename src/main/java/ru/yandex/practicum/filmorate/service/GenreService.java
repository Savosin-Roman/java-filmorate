package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;

    public List<Genre> findAll() {
        log.debug("Получение всех жанров");
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbc.query(sql, genreRowMapper);
    }

    public Genre findById(Long id) {
        log.debug("Поиск жанра с ID: {}", id);
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            return jdbc.queryForObject(sql, genreRowMapper, id);
        } catch (Exception e) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
    }
}