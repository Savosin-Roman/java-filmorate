package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.LikeDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final LikeDbStorage likeStorage;

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

    public void addLike(Long filmId, Long userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);

        filmStorage.findById(filmId);
        userStorage.findById(userId);

        if (likeStorage.userLikedFilm(filmId, userId)) {
            throw new ConditionsNotMetException("Пользователь уже поставил лайк этому фильму");
        }

        likeStorage.addLike(filmId, userId);
        log.info("Лайк добавлен");
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Пользователь {} убирает лайк у фильма {}", userId, filmId);

        // Проверяем существование
        filmStorage.findById(filmId);
        userStorage.findById(userId);

        if (!likeStorage.userLikedFilm(filmId, userId)) {
            throw new ConditionsNotMetException("Пользователь не ставил лайк этому фильму");
        }

        likeStorage.removeLike(filmId, userId);
        log.info("Лайк удален");
    }

    public List<Film> getPopular(int count) {
        log.info("Запрос на получение {} самых популярных фильмов", count);
        return filmStorage.getPopular(count);
    }
}