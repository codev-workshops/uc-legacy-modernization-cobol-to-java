package com.carddemo.online.controller;

import com.carddemo.online.config.SecurityConfig;
import com.carddemo.online.dto.MenuItem;
import com.carddemo.online.security.JwtAuthenticationFilter;
import com.carddemo.online.security.JwtUtil;
import com.carddemo.online.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MenuController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class, BatchAutoConfiguration.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "USER")
    void getMenu_asRegularUser_returnsRegularItems() throws Exception {
        List<MenuItem> items = List.of(
                new MenuItem(1, "Account View", "COACTVWC"),
                new MenuItem(2, "Account Update", "COACTUPC")
        );
        when(menuService.getMenuItems("U")).thenReturn(items);

        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].option").value(1))
                .andExpect(jsonPath("$[0].name").value("Account View"))
                .andExpect(jsonPath("$[0].programId").value("COACTVWC"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMenu_asAdmin_returnsAdminItems() throws Exception {
        List<MenuItem> items = List.of(
                new MenuItem(1, "Account View", "COACTVWC"),
                new MenuItem(2, "User List (Security)", "COUSR00C")
        );
        when(menuService.getMenuItems("A")).thenReturn(items);

        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getMenu_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isUnauthorized());
    }
}
