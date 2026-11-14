package com.orbit.util;

import com.orbit.exception.AppException;

import java.util.Set;

public final class PageValidator {

    public static final Set<Integer> ALLOWED_LIMITS = Set.of(10, 25, 50, 100);
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 10;

    private PageValidator() {
    }

    public static int validatePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int validateLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (!ALLOWED_LIMITS.contains(limit)) {
            throw new AppException(AppUtils.Status.INVALID_PAGE_LIMIT, limit);
        }
        return limit;
    }
}
