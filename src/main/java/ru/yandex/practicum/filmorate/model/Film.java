package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Длина описания должна быть не больше 200 символов")
    private String description;

    @NotNull(message = "Дата должна быть указана")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность должна быть указана")
    private Duration duration;

    @AssertTrue(message = "Продолжительность фильма должна быть положительным числом")
    private boolean isDurationPositive() {
        return duration != null && duration.isPositive();
    }
}
