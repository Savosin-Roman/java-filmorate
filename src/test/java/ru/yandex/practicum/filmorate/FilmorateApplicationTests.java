package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FilmorateApplicationTests {

    private Validator validator;
    private FilmController filmController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        filmController = new FilmController();
        userController = new UserController();
    }

    @Test
    void testValidFilm() {
        Film film = createValidFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFilmNameBlank() {
        Film film = createValidFilm();
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        boolean hasNameError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertTrue(hasNameError);
    }

    @Test
    void testFilmDescriptionTooLong() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        boolean hasDescriptionError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description"));
        assertTrue(hasDescriptionError);
    }

    @Test
    void testFilmReleaseDateNull() {
        Film film = createValidFilm();
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        boolean hasDateError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate"));
        assertTrue(hasDateError);
    }

    @Test
    void testFilmDurationNull() {
        Film film = createValidFilm();
        film.setDuration(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        boolean hasDurationError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration"));
        assertTrue(hasDurationError);
    }

    @Test
    void testFilmDurationZero() {
        Film film = createValidFilm();
        film.setDuration(Duration.ZERO);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительным числом",
                violations.iterator().next().getMessage());
    }

    @Test
    void testFilmDurationNegative() {
        Film film = createValidFilm();
        film.setDuration(Duration.ofMinutes(-10));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Проверяем, что есть ошибка с нужным сообщением
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительным числом",
                violations.iterator().next().getMessage());
    }

    @Test
    void testValidUser() {
        User user = createValidUser();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUserEmailBlank() {
        User user = createValidUser();
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(hasEmailError);
    }

    @Test
    void testUserEmailInvalid() {
        User user = createValidUser();
        user.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(hasEmailError);
    }

    @Test
    void testUserLoginBlank() {
        User user = createValidUser();
        user.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        // Проверяем, что есть ошибка для поля login
        boolean hasLoginError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login"));
        assertTrue(hasLoginError);
    }

    @Test
    void testUserLoginWithSpaces() {
        User user = createValidUser();
        user.setLogin("user 123");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        boolean hasLoginError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login"));
        assertTrue(hasLoginError);
    }

    @Test
    void testUserBirthdayInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        boolean hasBirthdayError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday"));
        assertTrue(hasBirthdayError);
    }

    @Test
    void testUserNameCanBeNull() {
        User user = createValidUser();
        user.setName(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateFilmSuccess() {
        Film film = createValidFilm();
        Film created = filmController.create(film);

        assertNotNull(created.getId());
        assertEquals("Матрица", created.getName());
        assertEquals(LocalDate.of(1999, 3, 31), created.getReleaseDate());
    }

    @Test
    void testCreateFilmWithInvalidDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1890, 1, 1));

        assertThrows(DateValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    void testGetAllFilms() {
        Film film1 = createValidFilm();
        filmController.create(film1);

        Film film2 = createValidFilm();
        film2.setName("Интерстеллар");
        film2.setReleaseDate(LocalDate.of(2014, 11, 7));
        filmController.create(film2);

        Collection<Film> allFilms = filmController.findAll();
        assertEquals(2, allFilms.size());
    }

    @Test
    void testUpdateFilmSuccess() {
        Film created = filmController.create(createValidFilm());

        Film updatedFilm = new Film();
        updatedFilm.setId(created.getId());
        updatedFilm.setName("Матрица 2");
        updatedFilm.setDescription("Продолжение");
        updatedFilm.setReleaseDate(LocalDate.of(2003, 5, 15));
        updatedFilm.setDuration(Duration.ofMinutes(138));

        Film result = filmController.update(updatedFilm);

        assertEquals("Матрица 2", result.getName());
        assertEquals(LocalDate.of(2003, 5, 15), result.getReleaseDate());
    }

    @Test
    void testUpdateFilmNotFound() {
        Film film = createValidFilm();
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> {
            filmController.update(film);
        });
    }

    @Test
    void testUpdateFilmWithInvalidDate() {
        Film created = filmController.create(createValidFilm());
        created.setReleaseDate(LocalDate.of(1890, 1, 1));

        assertThrows(DateValidationException.class, () -> {
            filmController.update(created);
        });
    }

    @Test
    void testCreateUserSuccess() {
        User user = createValidUser();
        User created = userController.create(user);

        assertNotNull(created.getId());
        assertEquals("user@mail.ru", created.getEmail());
        assertEquals("user123", created.getLogin());
    }

    @Test
    void testCreateUserWithEmptyName_ShouldSetLogin() {
        User user = createValidUser();
        user.setName("");

        User created = userController.create(user);
        assertEquals("user123", created.getName());
    }

    @Test
    void testCreateUserWithNullName_ShouldSetLogin() {
        User user = createValidUser();
        user.setName(null);

        User created = userController.create(user);
        assertEquals("user123", created.getName());
    }

    @Test
    void testGetAllUsers() {
        User user1 = createValidUser();
        userController.create(user1);

        User user2 = createValidUser();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user456");
        userController.create(user2);

        Collection<User> allUsers = userController.findAll();
        assertEquals(2, allUsers.size());
    }

    @Test
    void testUpdateUserSuccess() {
        User created = userController.create(createValidUser());

        User updatedUser = new User();
        updatedUser.setId(created.getId());
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("newlogin");
        updatedUser.setName("Новое имя");
        updatedUser.setBirthday(LocalDate.of(1995, 5, 5));

        User result = userController.update(updatedUser);

        assertEquals("new@mail.ru", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("Новое имя", result.getName());
    }

    @Test
    void testUpdateUserWithEmptyName_ShouldSetLogin() {
        User created = userController.create(createValidUser());
        created.setName("");

        User result = userController.update(created);
        assertEquals("user123", result.getName());
    }

    @Test
    void testUpdateUserNotFound() {
        User user = createValidUser();
        user.setId(999L);

        assertThrows(NotFoundException.class, () -> {
            userController.update(user);
        });
    }

    @Test
    void testUpdateUserWithoutId() {
        User user = createValidUser();
        user.setId(null);

        assertThrows(ConditionsNotMetException.class, () -> {
            userController.update(user);
        });
    }

    @Test
    void testFullFlow() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Космическая эпопея");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(Duration.ofMinutes(169));

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());

        Collection<Film> allFilms = filmController.findAll();
        assertEquals(1, allFilms.size());

        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("spaceman");
        user.setName("Купер");
        user.setBirthday(LocalDate.of(1970, 1, 1));

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());

        Collection<User> allUsers = userController.findAll();
        assertEquals(1, allUsers.size());
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Фильм о реальности");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(Duration.ofMinutes(136));
        return film;
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user123");
        user.setName("Иван Петров");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}