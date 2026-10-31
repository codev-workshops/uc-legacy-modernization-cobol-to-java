      *> Loads a 300-byte ASCII account unload into an indexed file
      *> keyed on the first 11 bytes (the IDCAMS REPRO step of ACCTFILE).
       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOADKSDS.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-FILE ASSIGN TO INFILE
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT KSDS-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS RANDOM
                  RECORD KEY IS KSDS-KEY
                  FILE STATUS IS KSDS-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  IN-FILE.
       01  IN-REC                PIC X(300).
       FD  KSDS-FILE.
       01  KSDS-REC.
           05  KSDS-KEY          PIC 9(11).
           05  KSDS-DATA         PIC X(289).
       WORKING-STORAGE SECTION.
       01  KSDS-STATUS           PIC XX.
       01  EOF-SW                PIC X VALUE 'N'.
       PROCEDURE DIVISION.
           OPEN INPUT IN-FILE OUTPUT KSDS-FILE
           PERFORM UNTIL EOF-SW = 'Y'
             READ IN-FILE
               AT END MOVE 'Y' TO EOF-SW
               NOT AT END
                 MOVE IN-REC TO KSDS-REC
                 WRITE KSDS-REC
                 IF KSDS-STATUS NOT = '00'
                   DISPLAY 'LOADKSDS WRITE STATUS ' KSDS-STATUS
                   STOP RUN RETURNING 8
                 END-IF
             END-READ
           END-PERFORM
           CLOSE IN-FILE KSDS-FILE
           STOP RUN.
