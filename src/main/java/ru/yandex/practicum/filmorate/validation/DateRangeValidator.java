package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<DateRange, LocalDate> {

    private LocalDate minDate;
    private LocalDate maxDate;

    @Override
    public void initialize(DateRange constraintAnnotation) {
        if (!constraintAnnotation.min().isEmpty()) {
            this.minDate = LocalDate.parse(constraintAnnotation.min());
        }
        if (!constraintAnnotation.max().isEmpty()) {
            this.maxDate = LocalDate.parse(constraintAnnotation.max());
        }
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true;
        }

        if (minDate != null && date.isBefore(minDate)) {
            return false;
        }

        if (maxDate != null && date.isAfter(maxDate)) {
            return false;
        }
        return true;
    }
}
