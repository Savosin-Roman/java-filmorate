package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        log.info("Создание нового фильма: {}", film.getName());
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.info("Обновление фильма с ID: {}", film.getId());
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        log.info("Поиск фильма с ID: {}", id);
        return filmStorage.findById(id);
    }

    public void delete(Long id) {
        log.info("Удаление фильма с ID: {}", id);
        filmStorage.delete(id);
    }

    public List<Film> findAll() {
        log.info("Получение всех фильмов");
        return filmStorage.findAll();
    }

    // Методы для работы с лайками
    public void addLike(Long filmId, Long userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);

        // Проверяем существование фильма и пользователя
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId); // Проверка существования пользователя

        if (film.getLikes().contains(userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            throw new ConditionsNotMetException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        log.info("Лайк добавлен. Теперь у фильма {} лайков: {}", filmId, film.getLikes().size());
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Пользователь {} убирает лайк у фильма {}", userId, filmId);

        // Проверяем существование фильма и пользователя
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId); // Проверка существования пользователя

        if (!film.getLikes().contains(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new ConditionsNotMetException("Пользователь не ставил лайк этому фильму");
        }

        film.getLikes().remove(userId);
        log.info("Лайк удален. Теперь у фильма {} лайков: {}", filmId, film.getLikes().size());
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Запрос на получение {} самых популярных фильмов", count);

        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}