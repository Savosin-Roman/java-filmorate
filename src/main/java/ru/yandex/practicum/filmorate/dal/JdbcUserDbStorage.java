package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcUserDbStorage implements UserDbStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    @Override
    public User create(User user) {
        log.info("Создание пользователя в БД: {}", user.getLogin());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить пользователя");
        }

        user.setId(id);
        log.info("Пользователь создан с ID: {}", id);
        return findById(id);
    }

    @Override
    public User update(User user) {
        log.info("Обновление пользователя в БД с ID: {}", user.getId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int rowsUpdated = jdbc.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        log.info("Пользователь с ID {} обновлен", user.getId());
        return findById(user.getId());
    }

    @Override
    public User findById(Long id) {
        log.debug("Поиск пользователя с ID: {}", id);

        String sql = "SELECT * FROM users WHERE user_id = ?";
        User user = jdbc.query(sql, rs -> {
            if (rs.next()) {
                return userRowMapper.mapRow(rs, rs.getRow());
            }
            return null;
        }, id);

        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }

        return user;
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление пользователя с ID: {}", id);

        String deleteFriendsSql = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?";
        jdbc.update(deleteFriendsSql, id, id);

        String deleteLikesSql = "DELETE FROM likes WHERE user_id = ?";
        jdbc.update(deleteLikesSql, id);

        String sql = "DELETE FROM users WHERE user_id = ?";
        int rowsDeleted = jdbc.update(sql, id);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }

        log.info("Пользователь с ID {} удален", id);
    }

    @Override
    public List<User> findAll() {
        log.debug("Получение всех пользователей из БД");
        String sql = "SELECT * FROM users ORDER BY user_id";
        return jdbc.query(sql, userRowMapper);
    }

    @Override
    public List<User> getFriends(Long id) {
        log.debug("Получение друзей пользователя с ID: {}", id);

        findById(id);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ? AND f.confirmed = TRUE";

        return jdbc.query(sql, userRowMapper, id);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        log.debug("Получение общих друзей пользователей {} и {}", userId1, userId2);

        findById(userId1);
        findById(userId2);

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
    @Override
    public Optional<User> findUserById(Long id) {
        log.debug("Поиск пользователя с ID: {}", id);

        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbc.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}