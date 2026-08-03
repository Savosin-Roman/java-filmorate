package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface LikeDbStorage {

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    int getLikesCount(Long filmId);

    List<User> getUsersWhoLiked(Long filmId);

    List<Long> getUserIdsWhoLiked(Long filmId);

    boolean userLikedFilm(Long filmId, Long userId);

    List<Long> getMostLikedFilmIds(int count);
}