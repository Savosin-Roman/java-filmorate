package ru.yandex.practicum.filmorate.exception;

public class DateValidationException extends ValidationException {
    public DateValidationException() {
        super("Дата должна быть не раньше 28 декабря 1895 года");
    }
}