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
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        assertTrue(violations.isEmpty(), "Валидный фильм не должен иметь ошибок");
    }

    @Test
    void testFilmNameBlank() {
        Film film = createValidFilm();
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название не должно быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void testFilmNameNull() {
        Film film = createValidFilm();
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название не должно быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void testFilmDescriptionTooLong() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Длина описания должна быть не больше 200 символов",
                violations.iterator().next().getMessage());
    }

    @Test
    void testFilmDescriptionMaxLength() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Описание длиной 200 символов должно проходить валидацию");
    }

    @Test
    void testFilmDescriptionEmpty() {
        Film film = createValidFilm();
        film.setDescription("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Пустое описание допустимо");
    }

    @Test
    void testFilmReleaseDateNull() {
        Film film = createValidFilm();
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Дата должна быть указана", violations.iterator().next().getMessage());
    }

    @Test
    void testFilmDurationNull() {
        Film film = createValidFilm();
        film.setDuration(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());  // ← теперь будет false (есть ошибка)

        boolean hasDurationError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration"));
        assertTrue(hasDurationError);
    }

    @Test
    void testFilmDurationZero() {
        Film film = createValidFilm();
        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность должна быть положительным числом",
                violations.iterator().next().getMessage());
    }

    @Test
    void testFilmDurationNegative() {
        Film film = createValidFilm();
        film.setDuration(-10);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность должна быть положительным числом",
                violations.iterator().next().getMessage());
    }

    @Test
    void testFilmDurationPositive() {
        Film film = createValidFilm();
        film.setDuration(1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Продолжительность > 0 допустима");
    }

    @Test
    void testValidUser() {
        User user = createValidUser();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь ошибок");
    }

    @Test
    void testUserEmailBlank() {
        User user = createValidUser();
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void testUserEmailNull() {
        User user = createValidUser();
        user.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void testUserEmailInvalid() {
        User user = createValidUser();
        user.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Некорректный формат email", violations.iterator().next().getMessage());
    }

    @Test
    void testUserEmailValid() {
        User user = createValidUser();
        user.setEmail("user@mail.ru");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Корректный email должен проходить валидацию");
    }

    @Test
    void testUserLoginBlank() {
        User user = createValidUser();
        user.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        boolean hasLoginError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login"));
        assertTrue(hasLoginError);
    }

    @Test
    void testUserLoginNull() {
        User user = createValidUser();
        user.setLogin(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

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
        assertEquals("Логин не должен содержать пробелы",
                violations.iterator().next().getMessage());
    }

    @Test
    void testUserLoginValid() {
        User user = createValidUser();
        user.setLogin("user123");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Корректный логин должен проходить валидацию");
    }

    @Test
    void testUserBirthdayInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем",
                violations.iterator().next().getMessage());
    }

    @Test
    void testUserBirthdayToday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Сегодняшняя дата допустима с @PastOrPresent");
    }

    @Test
    void testUserBirthdayPast() {
        User user = createValidUser();
        user.setBirthday(LocalDate.of(1990, 1, 1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Прошлая дата допустима");
    }

    @Test
    void testUserNameCanBeNull() {
        User user = createValidUser();
        user.setName(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Имя может быть null");
    }

    @Test
    void testUserNameCanBeEmpty() {
        User user = createValidUser();
        user.setName("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Имя может быть пустым");
    }


    @Test
    void testCreateFilmSuccess() {
        Film film = createValidFilm();
        Film created = filmController.create(film);

        assertNotNull(created.getId());
        assertEquals("Матрица", created.getName());
        assertEquals("Фильм о реальности", created.getDescription());
        assertEquals(LocalDate.of(1999, 3, 31), created.getReleaseDate());
        assertEquals(136, created.getDuration());
    }

    @Test
    void testCreateFilmWithMinimumDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        Film created = filmController.create(film);
        assertNotNull(created.getId());
        assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
    }

    @Test
    void testGetAllFilmsEmpty() {
        Collection<Film> allFilms = filmController.findAll();
        assertTrue(allFilms.isEmpty(), "Изначально список фильмов должен быть пуст");
    }

    @Test
    void testGetAllFilmsWithData() {
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
        updatedFilm.setDescription("Продолжение культового фильма");
        updatedFilm.setReleaseDate(LocalDate.of(2003, 5, 15));
        updatedFilm.setDuration(138);

        Film result = filmController.update(updatedFilm);

        assertEquals("Матрица 2", result.getName());
        assertEquals("Продолжение культового фильма", result.getDescription());
        assertEquals(LocalDate.of(2003, 5, 15), result.getReleaseDate());
        assertEquals(138, result.getDuration());
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
    void testUpdateFilmWithoutId() {
        Film film = createValidFilm();
        film.setId(null);

        assertThrows(ConditionsNotMetException.class, () -> {
            filmController.update(film);
        });
    }

    @Test
    void testCreateMultipleFilmsWithUniqueIds() {
        Film film1 = filmController.create(createValidFilm());
        Film film2 = filmController.create(createValidFilm());
        Film film3 = filmController.create(createValidFilm());

        assertNotEquals(film1.getId(), film2.getId());
        assertNotEquals(film2.getId(), film3.getId());
        assertNotEquals(film1.getId(), film3.getId());
    }

    @Test
    void testCreateUserSuccess() {
        User user = createValidUser();
        User created = userController.create(user);

        assertNotNull(created.getId());
        assertEquals("user@mail.ru", created.getEmail());
        assertEquals("user123", created.getLogin());
        assertEquals("Иван Петров", created.getName());
        assertEquals(LocalDate.of(1990, 1, 1), created.getBirthday());
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
    void testCreateUserWithName() {
        User user = createValidUser();
        user.setName("Пётр Сидоров");

        User created = userController.create(user);
        assertEquals("Пётр Сидоров", created.getName());
    }

    @Test
    void testGetAllUsersEmpty() {
        Collection<User> allUsers = userController.findAll();
        assertTrue(allUsers.isEmpty(), "Изначально список пользователей должен быть пуст");
    }

    @Test
    void testGetAllUsersWithData() {
        User user1 = createValidUser();
        userController.create(user1);

        User user2 = createValidUser();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user456");
        user2.setName("Анна Иванова");
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
        assertEquals(LocalDate.of(1995, 5, 5), result.getBirthday());
    }

    @Test
    void testUpdateUserWithEmptyName_ShouldSetLogin() {
        User created = userController.create(createValidUser());
        created.setName("");

        User result = userController.update(created);
        assertEquals("user123", result.getName());
    }

    @Test
    void testUpdateUserWithNullName_ShouldSetLogin() {
        User created = userController.create(createValidUser());
        created.setName(null);

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
    void testCreateMultipleUsersWithUniqueIds() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());
        User user3 = userController.create(createValidUser());

        assertNotEquals(user1.getId(), user2.getId());
        assertNotEquals(user2.getId(), user3.getId());
        assertNotEquals(user1.getId(), user3.getId());
    }


    @Test
    void testFullFlow() {
        // 1. Создаём фильм
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Космическая эпопея Кристофера Нолана");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());
        assertEquals("Интерстеллар", createdFilm.getName());

        // 2. Проверяем, что фильм сохранился в списке
        Collection<Film> allFilms = filmController.findAll();
        assertEquals(1, allFilms.size());
        assertEquals("Интерстеллар", allFilms.iterator().next().getName());

        // 3. Создаём пользователя
        User user = new User();
        user.setEmail("cooper@nasa.gov");
        user.setLogin("cooper");
        user.setName("Купер");
        user.setBirthday(LocalDate.of(1970, 1, 1));

        User createdUser = userController.create(user);
        assertNotNull(createdUser.getId());
        assertEquals("cooper", createdUser.getLogin());

        // 4. Проверяем, что пользователь сохранился в списке
        Collection<User> allUsers = userController.findAll();
        assertEquals(1, allUsers.size());
        assertEquals("cooper", allUsers.iterator().next().getLogin());

        // 5. Обновляем фильм
        createdFilm.setName("Интерстеллар: Перезагрузка");
        createdFilm.setDuration(170);
        Film updatedFilm = filmController.update(createdFilm);
        assertEquals("Интерстеллар: Перезагрузка", updatedFilm.getName());
        assertEquals(170, updatedFilm.getDuration());

        // 6. Обновляем пользователя
        createdUser.setEmail("cooper@interstellar.com");
        createdUser.setName("Купер (обновлён)");
        User updatedUser = userController.update(createdUser);
        assertEquals("cooper@interstellar.com", updatedUser.getEmail());
        assertEquals("Купер (обновлён)", updatedUser.getName());

        // 7. Проверяем финальное состояние
        Collection<Film> finalFilms = filmController.findAll();
        Collection<User> finalUsers = userController.findAll();

        assertEquals(1, finalFilms.size());
        assertEquals(1, finalUsers.size());
        assertEquals("Интерстеллар: Перезагрузка", finalFilms.iterator().next().getName());
        assertEquals("cooper@interstellar.com", finalUsers.iterator().next().getEmail());
    }

    @Test
    void testFilmWithMaxDescriptionLength() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));
        Film created = filmController.create(film);
        assertEquals(200, created.getDescription().length());
    }

    @Test
    void testFilmWithVeryLongName() {
        Film film = createValidFilm();
        film.setName("a".repeat(1000));
        Film created = filmController.create(film);
        assertEquals(1000, created.getName().length());
    }

    @Test
    void testUserWithVeryLongLogin() {
        User user = createValidUser();
        user.setLogin("a".repeat(100));
        User created = userController.create(user);
        assertEquals(100, created.getLogin().length());
    }

    @Test
    void testUserWithVeryLongName() {
        User user = createValidUser();
        user.setName("a".repeat(100));
        User created = userController.create(user);
        assertEquals(100, created.getName().length());
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Фильм о реальности");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
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