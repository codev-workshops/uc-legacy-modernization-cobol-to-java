package com.carddemo.online.service;

import com.carddemo.online.dto.MenuItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Replaces COMEN01C.cbl (regular menu) and COADM01C.cbl (admin menu).
 * Menu items derived from COMEN02Y.cpy (11 regular items) and COADM02Y.cpy (6 admin items).
 */
@Service
public class MenuService {

    private static final List<MenuItem> REGULAR_MENU_ITEMS;
    private static final List<MenuItem> ADMIN_MENU_ITEMS;

    static {
        List<MenuItem> regular = new ArrayList<>();
        regular.add(new MenuItem(1, "Account View", "COACTVWC"));
        regular.add(new MenuItem(2, "Account Update", "COACTUPC"));
        regular.add(new MenuItem(3, "Credit Card List", "COCRDLIC"));
        regular.add(new MenuItem(4, "Credit Card View", "COCRDSLC"));
        regular.add(new MenuItem(5, "Credit Card Update", "COCRDUPC"));
        regular.add(new MenuItem(6, "Transaction List", "COTRN00C"));
        regular.add(new MenuItem(7, "Transaction View", "COTRN01C"));
        regular.add(new MenuItem(8, "Transaction Add", "COTRN02C"));
        regular.add(new MenuItem(9, "Transaction Reports", "CORPT00C"));
        regular.add(new MenuItem(10, "Bill Payment", "COBIL00C"));
        regular.add(new MenuItem(11, "Pending Authorization View", "COPAUS0C"));
        REGULAR_MENU_ITEMS = Collections.unmodifiableList(regular);

        List<MenuItem> admin = new ArrayList<>();
        admin.add(new MenuItem(1, "User List (Security)", "COUSR00C"));
        admin.add(new MenuItem(2, "User Add (Security)", "COUSR01C"));
        admin.add(new MenuItem(3, "User Update (Security)", "COUSR02C"));
        admin.add(new MenuItem(4, "User Delete (Security)", "COUSR03C"));
        admin.add(new MenuItem(5, "Transaction Type List/Update (Db2)", "COTRTLIC"));
        admin.add(new MenuItem(6, "Transaction Type Maintenance (Db2)", "COTRTUPC"));
        ADMIN_MENU_ITEMS = Collections.unmodifiableList(admin);
    }

    public List<MenuItem> getMenuItems(String userType) {
        List<MenuItem> items = new ArrayList<>(REGULAR_MENU_ITEMS);
        if ("A".equals(userType)) {
            items.addAll(ADMIN_MENU_ITEMS);
        }
        return Collections.unmodifiableList(items);
    }
}
