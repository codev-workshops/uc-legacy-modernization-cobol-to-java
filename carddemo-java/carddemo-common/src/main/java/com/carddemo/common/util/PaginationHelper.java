package com.carddemo.common.util;

/**
 * Pagination request DTO for page/size/sort parameters.
 */
public class PaginationHelper {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final int page;
    private final int size;
    private final String sortBy;
    private final String sortDirection;

    public PaginationHelper() {
        this(DEFAULT_PAGE, DEFAULT_SIZE, null, "ASC");
    }

    public PaginationHelper(int page, int size) {
        this(page, size, null, "ASC");
    }

    public PaginationHelper(int page, int size, String sortBy, String sortDirection) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be at least 1");
        }
        this.page = page;
        this.size = Math.min(size, MAX_SIZE);
        this.sortBy = sortBy;
        this.sortDirection = normalizeSortDirection(sortDirection);
    }

    private static String normalizeSortDirection(String direction) {
        if (direction == null) {
            return "ASC";
        }
        String upper = direction.trim().toUpperCase();
        if ("ASC".equals(upper) || "DESC".equals(upper)) {
            return upper;
        }
        throw new IllegalArgumentException("Sort direction must be ASC or DESC, got: " + direction);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public int getOffset() {
        return page * size;
    }
}
