package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserDbStorage userStorage;    // ← JDBC
    private final FriendDbStorage friendStorage; // ← JDBC

    public List<User> findAll() {
        log.debug("Запрос всех пользователей");
        return userStorage.findAll();
    }

    public User findById(long id) {
        log.debug("Поиск пользователя с ID: {}", id);
        return userStorage.findById(id);
    }

    public User create(User user) {
        log.info("Создание пользователя: {}", user.getLogin());
        validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        log.info("Обновление пользователя с ID: {}", user.getId());
        validateUser(user);
        return userStorage.update(user);
    }

    private void validateUser(User user) {
        if (user.getLogin() != null && user.getLogin().contains(" ")) {
            throw new ConditionsNotMetException("Логин не должен содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void delete(Long id) {
        log.info("Удаление пользователя с ID: {}", id);
        userStorage.delete(id);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление в друзья {} пользователя {}", friendId, userId);
        friendStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаление из друзей {} пользователя {}", friendId, userId);
        friendStorage.removeFriend(userId, friendId);
    }

    public void confirmFriend(Long userId, Long friendId) {
        log.info("Подтверждение дружбы между {} и {}", userId, friendId);
        friendStorage.confirmFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.info("Получение списка друзей пользователя {}", userId);
        return friendStorage.getFriends(userId);
    }

    public List<User> getPendingRequests(Long userId) {
        log.info("Получение заявок в друзья пользователя {}", userId);
        return friendStorage.getPendingRequests(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Получение списка общих друзей {} и {}", userId, otherUserId);
        return friendStorage.getCommonFriends(userId, otherUserId);
    }
}