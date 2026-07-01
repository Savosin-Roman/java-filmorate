package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DateRange;

import java.time.LocalDate;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Длина описания должна быть не больше 200 символов")
    private String description;

    @NotNull(message = "Дата должна быть указана")
    @DateRange(min = "1895-12-28", message = "Дата выхода не может быть раньше {min}")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность должна быть указана")
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;  // ← продолжительность в минутах
}
