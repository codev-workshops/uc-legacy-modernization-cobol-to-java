package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardDemoCommareaTest {

    @Test
    void fieldCountMatchesCopybook() {
        // COCOM01Y.cpy has 16 non-FILLER fields (excluding 88-levels)
        assertEquals(16, CardDemoCommarea.class.getDeclaredFields().length);
    }

    @Test
    void canInstantiateWithValidData() {
        var commarea = new CardDemoCommarea();
        commarea.setCdemoFromTranid("CR01");
        commarea.setCdemoFromProgram("COCRDUPC");
        commarea.setCdemoToTranid("CR02");
        commarea.setCdemoToProgram("COCRDSLC");
        commarea.setCdemoUserId("ADMIN001");
        commarea.setCdemoUserType("A");
        commarea.setCdemoPgmContext(0);
        commarea.setCdemoCustId(123456789);
        commarea.setCdemoCustFname("John");
        commarea.setCdemoCustMname("M");
        commarea.setCdemoCustLname("Doe");
        commarea.setCdemoAcctId(12345678901L);
        commarea.setCdemoAcctStatus("Y");
        commarea.setCdemoCardNum(4111111111111111L);
        commarea.setCdemoLastMap("MAP001");
        commarea.setCdemoLastMapset("SET001");

        assertEquals("CR01", commarea.getCdemoFromTranid());
        assertEquals("A", commarea.getCdemoUserType());
        assertEquals(0, commarea.getCdemoPgmContext());
    }
}
