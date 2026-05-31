package com.carddemo.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaginationHelperTest {

    @Test
    void defaultConstructor_setsDefaults() {
        PaginationHelper helper = new PaginationHelper();
        assertEquals(0, helper.getPage());
        assertEquals(20, helper.getSize());
        assertNull(helper.getSortBy());
        assertEquals("ASC", helper.getSortDirection());
    }

    @Test
    void twoArgConstructor_setsPageAndSize() {
        PaginationHelper helper = new PaginationHelper(2, 50);
        assertEquals(2, helper.getPage());
        assertEquals(50, helper.getSize());
        assertNull(helper.getSortBy());
        assertEquals("ASC", helper.getSortDirection());
    }

    @Test
    void fullConstructor_setsAllFields() {
        PaginationHelper helper = new PaginationHelper(1, 25, "name", "DESC");
        assertEquals(1, helper.getPage());
        assertEquals(25, helper.getSize());
        assertEquals("name", helper.getSortBy());
        assertEquals("DESC", helper.getSortDirection());
    }

    @Test
    void size_cappedAtMax() {
        PaginationHelper helper = new PaginationHelper(0, 200);
        assertEquals(100, helper.getSize());
    }

    @Test
    void negativePage_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PaginationHelper(-1, 10));
    }

    @Test
    void zeroSize_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PaginationHelper(0, 0));
    }

    @Test
    void invalidSortDirection_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PaginationHelper(0, 10, "name", "INVALID"));
    }

    @Test
    void getOffset_calculatesCorrectly() {
        PaginationHelper helper = new PaginationHelper(3, 25);
        assertEquals(75, helper.getOffset());
    }

    @Test
    void getOffset_pageZero() {
        PaginationHelper helper = new PaginationHelper(0, 10);
        assertEquals(0, helper.getOffset());
    }

    @Test
    void sortDirection_caseInsensitive() {
        PaginationHelper helper = new PaginationHelper(0, 10, "id", "desc");
        assertEquals("DESC", helper.getSortDirection());
    }

    @Test
    void sortDirection_nullDefaultsToAsc() {
        PaginationHelper helper = new PaginationHelper(0, 10, "id", null);
        assertEquals("ASC", helper.getSortDirection());
    }
}
