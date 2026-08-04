package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendDbStorage {

    void addFriend(Long userId, Long friendId);

    void confirmFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getPendingRequests(Long userId);

    boolean areFriends(Long userId1, Long userId2);

    List<User> getCommonFriends(Long userId1, Long userId2);
}