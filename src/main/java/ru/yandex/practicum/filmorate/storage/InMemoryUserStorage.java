package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage{

    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    private long getNextId() {
        return ++currentId;
    }

    @Override
    public User create(User user) {
        log.info("Создание нового пользователя: {}", user.getLogin());

        user.setId(getNextId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено как логин: {}", user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь создан с ID: {}", user.getId());

        return user;
    }

    @Override
    public User update(User newUser) {
        log.info("Обновление пользователя с ID: {}", newUser.getId());

        if (newUser.getId() == null) {
            log.warn("Попытка обновления без указания ID");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с ID {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с ID " + newUser.getId() + " не найден");
        }

        oldUser.setLogin(newUser.getLogin());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setBirthday(newUser.getBirthday());

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            oldUser.setName(newUser.getLogin());
            log.debug("Имя обновлено на логин: {}", newUser.getLogin());
        } else {
            oldUser.setName(newUser.getName());
        }

        log.info("Пользователь с ID {} успешно обновлён", newUser.getId());
        return new User(oldUser);
    }

    @Override
    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return new User(user);
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление пользователя с ID: {}", id);
        if (users.remove(id) == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        log.info("Пользователь с ID {} удалён", id);
    }

    @Override
    public List<User> findAll() {
        return users.values().stream()
                .map(User::new)
                .collect(Collectors.toList());
    }
}
