package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcFriendDbStorage implements FriendDbStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    private final UserDbStorage userStorage;

    @Override
    public void addFriend(Long userId, Long friendId) {
        log.debug("Добавление друга {} пользователю {}", friendId, userId);

        userStorage.findById(userId);
        userStorage.findById(friendId);

        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        if (areFriends(userId, friendId)) {
            throw new ConditionsNotMetException("Пользователи уже являются друзьями");
        }

        String checkReverseSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ? AND confirmed = TRUE";
        Integer reverseCount = jdbc.queryForObject(checkReverseSql, Integer.class, friendId, userId);

        if (reverseCount != null && reverseCount > 0) {
            // Есть встречная заявка → подтверждаем дружбу
            confirmFriend(friendId, userId);
            return;
        }

        String checkExistingSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer existingCount = jdbc.queryForObject(checkExistingSql, Integer.class, userId, friendId);
        if (existingCount != null && existingCount > 0) {
            throw new ConditionsNotMetException("Заявка уже отправлена");
        }

        String sql = "INSERT INTO friends (user_id, friend_id, confirmed) VALUES (?, ?, FALSE)";
        jdbc.update(sql, userId, friendId);

        log.info("Заявка в друзья создана: {} -> {}", userId, friendId);
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        log.debug("Подтверждение дружбы между {} и {}", userId, friendId);

        userStorage.findById(userId);
        userStorage.findById(friendId);

        String checkSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ? AND confirmed = TRUE";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count == null || count == 0) {
            throw new ConditionsNotMetException("Заявка в друзья не найдена");
        }

        String updateSql = "UPDATE friends SET confirmed = TRUE WHERE user_id = ? AND friend_id = ?";
        jdbc.update(updateSql, userId, friendId);

        String insertReverseSql = "INSERT INTO friends (user_id, friend_id, confirmed) VALUES (?, ?, TRUE)";
        jdbc.update(insertReverseSql, friendId, userId);

        log.info("Дружба подтверждена между {} и {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление друга {} у пользователя {}", friendId, userId);

        userStorage.findById(userId);
        userStorage.findById(friendId);

        String sql = "DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        int rowsDeleted = jdbc.update(sql, userId, friendId, friendId, userId);

        if (rowsDeleted == 0) {
            log.warn("Дружба не найдена между {} и {}", userId, friendId);
        } else {
            log.info("Дружба удалена между {} и {}", userId, friendId);
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        userStorage.findById(userId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ? AND f.confirmed = TRUE";

        return jdbc.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getPendingRequests(Long userId) {
        userStorage.findById(userId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ? AND f.confirmed = FALSE";

        return jdbc.query(sql, userRowMapper, userId);
    }

    @Override
    public boolean areFriends(Long userId1, Long userId2) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ? AND confirmed = TRUE";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId1, userId2);
        return count != null && count > 0;
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        userStorage.findById(userId1);
        userStorage.findById(userId2);

        String sql = "SELECT u.* FROM users u " +
                "WHERE u.user_id IN (" +
                "    SELECT f1.friend_id FROM friends f1 " +
                "    WHERE f1.user_id = ? AND f1.confirmed = TRUE " +
                "    INTERSECT " +
                "    SELECT f2.friend_id FROM friends f2 " +
                "    WHERE f2.user_id = ? AND f2.confirmed = TRUE" +
                ")";

        return jdbc.query(sql, userRowMapper, userId1, userId2);
    }
}