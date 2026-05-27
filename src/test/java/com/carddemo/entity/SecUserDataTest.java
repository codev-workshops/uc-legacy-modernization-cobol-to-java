package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecUserDataTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CSUSR01Y.cpy has 5 non-FILLER fields
        assertEquals(5, SecUserData.class.getDeclaredFields().length);
    }

    @Test
    void passwordFieldExists() throws NoSuchFieldException {
        assertNotNull(SecUserData.class.getDeclaredField("secUsrPwd"));
    }

    @Test
    void canInstantiateWithValidData() {
        var user = new SecUserData();
        user.setSecUsrId("ADMIN001");
        user.setSecUsrFname("Admin");
        user.setSecUsrLname("User");
        user.setSecUsrPwd("password");
        user.setSecUsrType("A");

        assertEquals("ADMIN001", user.getSecUsrId());
        assertEquals("Admin", user.getSecUsrFname());
        assertEquals("A", user.getSecUsrType());
    }
}
