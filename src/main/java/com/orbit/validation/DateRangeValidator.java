package com.orbit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, HasDateRange> {

    @Override
    public boolean isValid(HasDateRange value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        LocalDateTime start = value.getStartDate();
        LocalDateTime end = value.getEndDate();

        if (start == null || end == null) {
            return true; // Single date validation handled by separate @NotNull if required
        }

        return !start.isAfter(end);
    }
}
