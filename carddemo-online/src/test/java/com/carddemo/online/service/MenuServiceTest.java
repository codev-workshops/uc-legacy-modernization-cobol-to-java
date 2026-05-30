package com.carddemo.online.service;

import com.carddemo.online.dto.MenuItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuServiceTest {

    private final MenuService menuService = new MenuService();

    @Test
    void getMenuItems_regularUser_returns11Items() {
        List<MenuItem> items = menuService.getMenuItems("U");

        assertEquals(11, items.size());
        assertEquals("Account View", items.get(0).getName());
        assertEquals("COACTVWC", items.get(0).getProgramId());
        assertEquals(1, items.get(0).getOption());
        assertEquals("Pending Authorization View", items.get(10).getName());
    }

    @Test
    void getMenuItems_adminUser_returns17Items() {
        List<MenuItem> items = menuService.getMenuItems("A");

        assertEquals(17, items.size());

        // First 11 are regular menu items
        assertEquals("Account View", items.get(0).getName());
        assertEquals("Transaction Reports", items.get(8).getName());
        assertEquals("CORPT00C", items.get(8).getProgramId());

        // Last 6 are admin items
        assertEquals("User List (Security)", items.get(11).getName());
        assertEquals("COUSR00C", items.get(11).getProgramId());
        assertEquals("Transaction Type Maintenance (Db2)", items.get(16).getName());
        assertEquals("COTRTUPC", items.get(16).getProgramId());
    }

    @Test
    void getMenuItems_nullUserType_returns11Items() {
        List<MenuItem> items = menuService.getMenuItems(null);
        assertEquals(11, items.size());
    }

    @Test
    void getMenuItems_regularUser_containsTransactionReports() {
        List<MenuItem> items = menuService.getMenuItems("U");
        boolean hasReports = items.stream()
                .anyMatch(i -> "Transaction Reports".equals(i.getName())
                        && "CORPT00C".equals(i.getProgramId()));
        assertTrue(hasReports);
    }

    @Test
    void getMenuItems_regularUser_containsBillPayment() {
        List<MenuItem> items = menuService.getMenuItems("U");
        boolean hasBillPay = items.stream()
                .anyMatch(i -> "Bill Payment".equals(i.getName()));
        assertTrue(hasBillPay);
    }
}
