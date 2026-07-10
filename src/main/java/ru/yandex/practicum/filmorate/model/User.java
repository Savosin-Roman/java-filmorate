package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    private Set<Long> friends  = new HashSet<>();;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public User(User other) {
        this.id = other.id;
        this.email = other.email;
        this.login = other.login;
        this.name = other.name;
        this.birthday = other.birthday;
        if (other.friends != null) {
            this.friends = new HashSet<>(other.friends);
        } else {
            this.friends = new HashSet<>();
        }
    }
}
