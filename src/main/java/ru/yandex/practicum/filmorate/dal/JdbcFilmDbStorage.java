package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcFilmDbStorage implements FilmDbStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Film create(Film film) {
        log.info("Создание фильма в БД: {}", film.getName());

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить фильм");
        }

        film.setId(id);

        // Сохраняем жанры
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(id, film.getGenres());
        }

        log.info("Фильм создан с ID: {}", id);
        return findById(id);
    }

    private void saveGenres(Long filmId, List<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbc.update(sql, filmId, genre.getId());
        }
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма в БД с ID: {}", film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        int rowsUpdated = jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteGenresSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        log.info("Фильм с ID {} обновлен", film.getId());
        return findById(film.getId());
    }

    @Override
    public Film findById(Long id) {
        log.debug("Поиск фильма с ID: {}", id);

        String sql = "SELECT f.*, m.mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";

        Film film = jdbc.query(sql, rs -> {
            if (rs.next()) {
                Film f = filmRowMapper.mapRow(rs, rs.getRow());
                return f;
            }
            return null;
        }, id);

        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }

        loadGenres(film);
        loadLikes(film);

        return film;
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.* FROM genres g " +
                "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";
        List<Genre> genres = jdbc.query(sql, genreRowMapper, film.getId());
        film.setGenres(genres);
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likedUserIds = jdbc.queryForList(sql, Long.class, film.getId());
        film.setLikedUserIds(likedUserIds);
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление фильма с ID: {}", id);

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteGenresSql, id);

        String deleteLikesSql = "DELETE FROM likes WHERE film_id = ?";
        jdbc.update(deleteLikesSql, id);

        String sql = "DELETE FROM films WHERE film_id = ?";
        int rowsDeleted = jdbc.update(sql, id);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }

        log.info("Фильм с ID {} удален", id);
    }

    @Override
    public List<Film> findAll() {
        log.debug("Получение всех фильмов из БД");

        String sql = "SELECT f.*, m.mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "ORDER BY f.film_id";

        List<Film> films = jdbc.query(sql, filmRowMapper);

        // Загружаем жанры и лайки для каждого фильма
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }

        return films;
    }

    @Override
    public List<Film> getPopular(int count) {
        log.debug("Получение {} популярных фильмов", count);

        String sql = "SELECT f.*, m.mpa_name, COUNT(l.user_id) as like_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY like_count DESC, f.film_id " +
                "LIMIT ?";

        List<Film> films = jdbc.query(sql, filmRowMapper, count);

        // Загружаем жанры и лайки для каждого фильма
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }

        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка от пользователя {} к фильму {}", userId, filmId);

        findById(filmId);

        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, filmId, userId);
        if (count != null && count > 0) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            return;
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);

        log.info("Лайк добавлен: фильм {}, пользователь {}", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка от пользователя {} к фильму {}", userId, filmId);

        findById(filmId);

        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int rowsDeleted = jdbc.update(sql, filmId, userId);

        if (rowsDeleted == 0) {
            log.warn("Лайк не найден: фильм {}, пользователь {}", filmId, userId);
        } else {
            log.info("Лайк удален: фильм {}, пользователь {}", filmId, userId);
        }
    }
}