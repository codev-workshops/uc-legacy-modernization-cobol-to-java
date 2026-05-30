package com.carddemo.online.controller;

import com.carddemo.online.dto.MenuItem;
import com.carddemo.online.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<List<MenuItem>> getMenu(Authentication authentication) {
        String userType = resolveUserType(authentication);
        List<MenuItem> items = menuService.getMenuItems(userType);
        return ResponseEntity.ok(items);
    }

    private String resolveUserType(Authentication authentication) {
        if (authentication == null) {
            return "U";
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        return isAdmin ? "A" : "U";
    }
}
