      ******************************************************************
      * Local stand-in for the LE abend service CEE3ABD.
      * Reports the abend code and ends the run unit with a non-zero
      * return code instead of a system abend.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEE3ABD.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-ABCODE               PIC 9(9) VALUE 999.
       LINKAGE SECTION.
       01  LK-ABCODE               PIC S9(9) BINARY.
       01  LK-TIMING               PIC S9(9) BINARY.
       PROCEDURE DIVISION USING LK-ABCODE LK-TIMING.
           IF NUMBER-OF-CALL-PARAMETERS > 0
               MOVE LK-ABCODE TO WS-ABCODE
           END-IF
           DISPLAY 'CEE3ABD: PROGRAM ABENDED WITH CODE U' WS-ABCODE
               UPON SYSERR
           MOVE 12 TO RETURN-CODE
           STOP RUN.
