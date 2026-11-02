      ******************************************************************
      * Batch step driver: emulates EXEC PGM=<program>,PARM='<parm>'.
      * Usage: pgmrun PROGRAM [PARM]
      * The PARM is passed as an LE-style halfword length + text area.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. PGMRUN.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-PROGRAM              PIC X(8)   VALUE SPACES.
       01  WS-PARM-TEXT            PIC X(100) VALUE SPACES.
       01  WS-JCL-PARM.
           05  WS-PARM-LENGTH      PIC S9(4) COMP VALUE 0.
           05  WS-PARM-DATA        PIC X(100) VALUE SPACES.
       PROCEDURE DIVISION.
           ACCEPT WS-PROGRAM FROM ARGUMENT-VALUE
           IF WS-PROGRAM = SPACES
               DISPLAY 'USAGE: pgmrun PROGRAM [PARM]' UPON SYSERR
               MOVE 16 TO RETURN-CODE
               STOP RUN
           END-IF
           ACCEPT WS-PARM-TEXT FROM ARGUMENT-VALUE
           MOVE WS-PARM-TEXT TO WS-PARM-DATA
           IF WS-PARM-TEXT NOT = SPACES
               COMPUTE WS-PARM-LENGTH =
                   FUNCTION LENGTH(FUNCTION TRIM(WS-PARM-TEXT TRAILING))
           END-IF
           CALL WS-PROGRAM USING WS-JCL-PARM
               ON EXCEPTION
                   DISPLAY 'PGMRUN: PROGRAM NOT FOUND: ' WS-PROGRAM
                       UPON SYSERR
                   MOVE 16 TO RETURN-CODE
           END-CALL
           STOP RUN.
