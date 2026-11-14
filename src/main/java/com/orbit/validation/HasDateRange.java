package com.orbit.validation;

import java.time.LocalDateTime;

public interface HasDateRange {
    LocalDateTime getStartDate();
    LocalDateTime getEndDate();
}
