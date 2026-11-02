      ******************************************************************
      * Local stand-in for app/asm/MVSWAIT.asm.
      * Waits for the given interval in hundredths of a second.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. MVSWAIT.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-SECONDS              PIC 9(8).
       LINKAGE SECTION.
       01  LK-WAIT-TIME            PIC 9(8) COMP.
       PROCEDURE DIVISION USING LK-WAIT-TIME.
           COMPUTE WS-SECONDS = LK-WAIT-TIME / 100
           CALL 'C$SLEEP' USING WS-SECONDS
           GOBACK.
