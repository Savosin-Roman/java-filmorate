package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Like {
    private Long filmId;
    private Long userId;
    private LocalDateTime createdAt;
}
