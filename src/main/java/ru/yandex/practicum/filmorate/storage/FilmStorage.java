package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film newFilm);

    Film findById(Long id);

    void delete(Long id);

    List<Film> findAll();
}
