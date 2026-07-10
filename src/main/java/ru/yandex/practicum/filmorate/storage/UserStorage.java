package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User create(User user);
    User update(User newUser);
    User findById(Long id);
    void delete(Long id);
    List<User> findAll();

}
