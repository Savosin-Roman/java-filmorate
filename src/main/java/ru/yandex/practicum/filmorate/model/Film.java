package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.DateRange;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
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
    private Integer duration;

    private Set<Long> likes = new HashSet<>();

    public Film(Film other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.releaseDate = other.releaseDate;
        this.duration = other.duration;

        if (other.likes != null) {
            this.likes = new HashSet<>(other.likes);
        } else {
            this.likes = new HashSet<>();
        }
    }
}
