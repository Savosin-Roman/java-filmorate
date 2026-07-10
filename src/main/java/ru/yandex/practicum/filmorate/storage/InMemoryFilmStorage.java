package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    private long getNextId() {
        return ++currentId;
    }

    @Override
    public Film create(Film film) {
        log.info("Создание нового фильма: {}", film.getName());
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан с ID: {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Обновление фильма с ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Попытка обновления без указания ID");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            log.warn("Фильм с ID {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с ID " + newFilm.getId() + " не найден");
        }

        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        log.info("Фильм с ID {} успешно обновлён", newFilm.getId());
        return oldFilm;
    }

    @Override
    public Film findById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return film;
    }

    @Override
    public void delete(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        films.remove(id);
        log.info("Фильм с ID {} удалён", id);
    }

    @Override
    public List<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return new ArrayList<>(films.values());
    }
}