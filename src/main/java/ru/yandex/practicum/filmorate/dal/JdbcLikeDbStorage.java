package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcLikeDbStorage implements LikeDbStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Override
    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка от пользователя {} к фильму {}", userId, filmId);

        filmStorage.findById(filmId);
        userStorage.findById(userId);

        if (userLikedFilm(filmId, userId)) {
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

        filmStorage.findById(filmId);
        userStorage.findById(userId);

        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int rowsDeleted = jdbc.update(sql, filmId, userId);

        if (rowsDeleted == 0) {
            log.warn("Лайк не найден: фильм {}, пользователь {}", filmId, userId);
        } else {
            log.info("Лайк удален: фильм {}, пользователь {}", filmId, userId);
        }
    }

    @Override
    public int getLikesCount(Long filmId) {
        filmStorage.findById(filmId);

        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId);
        return count != null ? count : 0;
    }

    @Override
    public List<User> getUsersWhoLiked(Long filmId) {
        filmStorage.findById(filmId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN likes l ON u.user_id = l.user_id " +
                "WHERE l.film_id = ?";

        return jdbc.query(sql, userRowMapper, filmId);
    }

    @Override
    public List<Long> getUserIdsWhoLiked(Long filmId) {
        filmStorage.findById(filmId);

        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return jdbc.queryForList(sql, Long.class, filmId);
    }

    @Override
    public boolean userLikedFilm(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    @Override
    public List<Long> getMostLikedFilmIds(int count) {
        String sql = "SELECT film_id FROM likes " +
                "GROUP BY film_id " +
                "ORDER BY COUNT(user_id) DESC " +
                "LIMIT ?";

        return jdbc.queryForList(sql, Long.class, count);
    }
}