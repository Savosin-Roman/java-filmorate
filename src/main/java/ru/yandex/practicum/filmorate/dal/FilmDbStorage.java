package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmDbStorage {
    Film create(Film film);

    Film update(Film newFilm);

    Film findById(Long id);

    void delete(Long id);

    List<Film> findAll();

    List<Film> getPopular(int count);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}