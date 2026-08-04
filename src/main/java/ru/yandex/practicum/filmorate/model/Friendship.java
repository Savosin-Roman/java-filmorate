package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Long userId;
    private Long friendID;
    private boolean confirmed;
    private LocalDateTime createdAt;
}
