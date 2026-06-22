package ru.yandex.practicum.filmorate.exception;

public class ConditionsNotMetException extends ValidationException {
    public ConditionsNotMetException(String message) {
        super(message);
    }
}
