package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        log.debug("Запрос всех пользователей");
        return userStorage.findAll();
    }

    public User findById(long id) {
        log.debug("Поиск пользователя с ID: {}", id);
        return userStorage.findById(id);
    }

    public User create(User user) {
        log.info("Создание пользователя: {}", user.getLogin());
        return userStorage.create(user);
    }

    public User update(User user) {
        log.info("Обновление пользователя с ID: {}", user.getId());
        return userStorage.update(user);
    }

    public void delete(Long id) {
        log.info("Удаление пользователя с ID: {}", id);
        userStorage.delete(id);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление в друзья {} пользователя {}", friendId, userId);

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавить себя в друзья");
        }

        if (user.getFriends().contains(friendId)) {
            throw new ConditionsNotMetException("Уже друзья");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} и {} друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаление из друзей {} пользователя {}", friendId, userId);

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        if (!user.getFriends().contains(friendId)) {
            throw new ConditionsNotMetException("Такого пользователя нет в друзьях");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("пользователь {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.info("Получение списка друзей пользователя {}", userId);

        User user = userStorage.findById(userId);
        Set<Long> friendIds = user.getFriends();

        if (friendIds.isEmpty()) {
            log.info("У пользователя {} нет друзей", userId);
            return Collections.emptyList();
        }

        List<User> friends = friendIds.stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());

        log.info("Найдено {} друзей у пользователя {}", friends.size(), userId);
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Получение списка общих друзей {} и {}", userId, otherUserId);

        User user = userStorage.findById(userId);
        User otherUser = userStorage.findById(otherUserId);

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        if (commonFriendIds.isEmpty()) {
            log.info("Общих друзей у пользователей {} и {} нет", userId, otherUserId);
            return Collections.emptyList();  //
        }

        List<User> commonFriends = commonFriendIds.stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());

        log.info("Получен список общих друзей {} и {}: {}", userId, otherUserId, commonFriends);
        return commonFriends;
    }
}

