package com.orbit.util;

import com.orbit.exception.AppException;
import lombok.Getter;

@Getter
public enum ProjectSortField {
    NAME("name"),
    CLIENT_NAME("clientName"),
    START_DATE("startDate"),
    END_DATE("endDate"),
    STATUS("status"),
    CREATED_AT("createdAt");

    private final String entityFieldName;

    ProjectSortField(String entityFieldName) {
        this.entityFieldName = entityFieldName;
    }

    public static String validateAndGet(String rawField) {
        if (rawField == null || rawField.trim().isEmpty()) {
            return CREATED_AT.getEntityFieldName();
        }
        String normalized = rawField.trim();
        for (ProjectSortField field : values()) {
            if (field.name().equalsIgnoreCase(normalized) ||
                field.entityFieldName.equalsIgnoreCase(normalized) ||
                field.name().replace("_", "").equalsIgnoreCase(normalized)) {
                return field.getEntityFieldName();
            }
        }
        throw new AppException(AppUtils.Status.INVALID_SORT_FIELD, rawField);
    }
}
