package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDbStorage {
    User create(User user);
    User update(User newUser);
    User findById(Long id);
    Optional<User> findUserById(Long id);  // ← Добавить этот метод
    void delete(Long id);
    List<User> findAll();
    List<User> getFriends(Long id);
    List<User> getCommonFriends(Long userId1, Long userId2);
}