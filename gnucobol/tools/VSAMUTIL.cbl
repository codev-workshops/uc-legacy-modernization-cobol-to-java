      ******************************************************************
      * Local stand-in for IDCAMS DEFINE CLUSTER + REPRO.
      *
      * Usage: vsamutil LOAD   DATASET   (DD SEQIN  = line-seq text file)
      *        vsamutil UNLOAD DATASET   (DD SEQOUT = line-seq text file)
      * The indexed (KSDS) file uses the same DD name as the batch
      * programs, e.g. ACCTDATA -> DD ACCTFILE.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. VSAMUTIL.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT SEQIN ASSIGN TO SEQIN
                  ORGANIZATION IS LINE SEQUENTIAL
                  FILE STATUS  IS WS-SEQ-STATUS.
           SELECT SEQOUT ASSIGN TO SEQOUT
                  ORGANIZATION IS LINE SEQUENTIAL
                  FILE STATUS  IS WS-SEQ-STATUS.
           SELECT K-ACCTDATA ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-ACCTDATA-KEY
                  FILE STATUS  IS WS-KSDS-STATUS.
           SELECT K-CARDDATA ASSIGN TO CARDFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-CARDDATA-KEY
                  FILE STATUS  IS WS-KSDS-STATUS.
           SELECT K-CUSTDATA ASSIGN TO CUSTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-CUSTDATA-KEY
                  FILE STATUS  IS WS-KSDS-STATUS.
           SELECT K-CARDXREF ASSIGN TO XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-CARDXREF-KEY
                  ALTERNATE RECORD KEY IS K-CARDXREF-AKEY
                      WITH DUPLICATES
                  FILE STATUS  IS WS-KSDS-STATUS.
           SELECT K-TCATBALF ASSIGN TO TCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-TCATBALF-KEY
                  FILE STATUS  IS WS-KSDS-STATUS.
           SELECT K-DISCGRP ASSIGN TO DISCGRP
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-DISCGRP-KEY
                  FILE STATUS  IS WS-KSDS-STATUS.
           SELECT K-TRANTYPE ASSIGN TO TRANTYPE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-TRANTYPE-KEY
                  FILE STATUS  IS WS-KSDS-STATUS.
           SELECT K-TRANCATG ASSIGN TO TRANCATG
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-TRANCATG-KEY
                  FILE STATUS  IS WS-KSDS-STATUS.
           SELECT K-TRANSACT ASSIGN TO TRANFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS DYNAMIC
                  RECORD KEY   IS K-TRANSACT-KEY
                  FILE STATUS  IS WS-KSDS-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  SEQIN.
       01  SEQIN-REC               PIC X(500).
       FD  SEQOUT.
       01  SEQOUT-REC              PIC X(500).
       FD  K-ACCTDATA.
       01  K-ACCTDATA-REC.
           05  K-ACCTDATA-KEY      PIC X(11).
           05  FILLER              PIC X(289).
       FD  K-CARDDATA.
       01  K-CARDDATA-REC.
           05  K-CARDDATA-KEY      PIC X(16).
           05  FILLER              PIC X(134).
       FD  K-CUSTDATA.
       01  K-CUSTDATA-REC.
           05  K-CUSTDATA-KEY      PIC X(9).
           05  FILLER              PIC X(491).
       FD  K-CARDXREF.
       01  K-CARDXREF-REC.
           05  K-CARDXREF-KEY      PIC X(16).
           05  FILLER              PIC X(9).
           05  K-CARDXREF-AKEY     PIC X(11).
           05  FILLER              PIC X(14).
       FD  K-TCATBALF.
       01  K-TCATBALF-REC.
           05  K-TCATBALF-KEY      PIC X(17).
           05  FILLER              PIC X(33).
       FD  K-DISCGRP.
       01  K-DISCGRP-REC.
           05  K-DISCGRP-KEY       PIC X(16).
           05  FILLER              PIC X(34).
       FD  K-TRANTYPE.
       01  K-TRANTYPE-REC.
           05  K-TRANTYPE-KEY      PIC X(2).
           05  FILLER              PIC X(58).
       FD  K-TRANCATG.
       01  K-TRANCATG-REC.
           05  K-TRANCATG-KEY      PIC X(6).
           05  FILLER              PIC X(54).
       FD  K-TRANSACT.
       01  K-TRANSACT-REC.
           05  K-TRANSACT-KEY      PIC X(16).
           05  FILLER              PIC X(334).
       WORKING-STORAGE SECTION.
       01  WS-ACTION               PIC X(8)  VALUE SPACES.
       01  WS-DATASET              PIC X(8)  VALUE SPACES.
       01  WS-SEQ-STATUS           PIC XX    VALUE '00'.
       01  WS-KSDS-STATUS          PIC XX    VALUE '00'.
       01  WS-EOF                  PIC X     VALUE 'N'.
       01  WS-RECORDS              PIC 9(9)  VALUE 0.
       01  WS-DUPLICATES           PIC 9(9)  VALUE 0.
       PROCEDURE DIVISION.
           ACCEPT WS-ACTION  FROM ARGUMENT-VALUE
           ACCEPT WS-DATASET FROM ARGUMENT-VALUE
           IF WS-ACTION NOT = 'LOAD' AND WS-ACTION NOT = 'UNLOAD'
               DISPLAY 'USAGE: vsamutil LOAD|UNLOAD DATASET'
                   UPON SYSERR
               MOVE 16 TO RETURN-CODE
               STOP RUN
           END-IF
           EVALUATE WS-DATASET
               WHEN 'ACCTDATA'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-ACCTDATA
                   ELSE
                       PERFORM UNLOAD-ACCTDATA
                   END-IF
               WHEN 'CARDDATA'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-CARDDATA
                   ELSE
                       PERFORM UNLOAD-CARDDATA
                   END-IF
               WHEN 'CUSTDATA'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-CUSTDATA
                   ELSE
                       PERFORM UNLOAD-CUSTDATA
                   END-IF
               WHEN 'CARDXREF'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-CARDXREF
                   ELSE
                       PERFORM UNLOAD-CARDXREF
                   END-IF
               WHEN 'TCATBALF'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-TCATBALF
                   ELSE
                       PERFORM UNLOAD-TCATBALF
                   END-IF
               WHEN 'DISCGRP'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-DISCGRP
                   ELSE
                       PERFORM UNLOAD-DISCGRP
                   END-IF
               WHEN 'TRANTYPE'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-TRANTYPE
                   ELSE
                       PERFORM UNLOAD-TRANTYPE
                   END-IF
               WHEN 'TRANCATG'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-TRANCATG
                   ELSE
                       PERFORM UNLOAD-TRANCATG
                   END-IF
               WHEN 'TRANSACT'
                   IF WS-ACTION = 'LOAD'
                       PERFORM LOAD-TRANSACT
                   ELSE
                       PERFORM UNLOAD-TRANSACT
                   END-IF
               WHEN OTHER
                   DISPLAY 'VSAMUTIL: UNKNOWN DATASET ' WS-DATASET
                       UPON SYSERR
                   MOVE 16 TO RETURN-CODE
                   STOP RUN
           END-EVALUATE
           DISPLAY 'VSAMUTIL: ' FUNCTION TRIM(WS-ACTION) ' '
               FUNCTION TRIM(WS-DATASET) ' RECORDS: ' WS-RECORDS
               ' DUPLICATES: ' WS-DUPLICATES
           IF WS-DUPLICATES > 0
               MOVE 4 TO RETURN-CODE
           END-IF
           STOP RUN.
       OPEN-SEQIN.
           OPEN INPUT SEQIN
           IF WS-SEQ-STATUS NOT = '00'
               DISPLAY 'VSAMUTIL: CANNOT OPEN SEQIN, STATUS '
                   WS-SEQ-STATUS UPON SYSERR
               MOVE 12 TO RETURN-CODE
               STOP RUN
           END-IF.
       CHECK-KSDS-OPEN.
           IF WS-KSDS-STATUS NOT = '00'
               DISPLAY 'VSAMUTIL: CANNOT OPEN KSDS, STATUS '
                   WS-KSDS-STATUS UPON SYSERR
               MOVE 12 TO RETURN-CODE
               STOP RUN
           END-IF.
       COUNT-WRITE.
           EVALUATE WS-KSDS-STATUS
               WHEN '00'
               WHEN '02'
                   ADD 1 TO WS-RECORDS
               WHEN '22'
                   ADD 1 TO WS-DUPLICATES
               WHEN OTHER
                   DISPLAY 'VSAMUTIL: WRITE FAILED, STATUS '
                       WS-KSDS-STATUS UPON SYSERR
                   MOVE 12 TO RETURN-CODE
                   STOP RUN
           END-EVALUATE.
       LOAD-ACCTDATA.
           OPEN OUTPUT K-ACCTDATA
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:300) TO K-ACCTDATA-REC
                       WRITE K-ACCTDATA-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-ACCTDATA.
       UNLOAD-ACCTDATA.
           OPEN INPUT K-ACCTDATA
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-ACCTDATA NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-ACCTDATA-REC TO SEQOUT-REC(1:300)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-ACCTDATA.
       LOAD-CARDDATA.
           OPEN OUTPUT K-CARDDATA
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:150) TO K-CARDDATA-REC
                       WRITE K-CARDDATA-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-CARDDATA.
       UNLOAD-CARDDATA.
           OPEN INPUT K-CARDDATA
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-CARDDATA NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-CARDDATA-REC TO SEQOUT-REC(1:150)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-CARDDATA.
       LOAD-CUSTDATA.
           OPEN OUTPUT K-CUSTDATA
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:500) TO K-CUSTDATA-REC
                       WRITE K-CUSTDATA-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-CUSTDATA.
       UNLOAD-CUSTDATA.
           OPEN INPUT K-CUSTDATA
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-CUSTDATA NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-CUSTDATA-REC TO SEQOUT-REC(1:500)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-CUSTDATA.
       LOAD-CARDXREF.
           OPEN OUTPUT K-CARDXREF
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:50) TO K-CARDXREF-REC
                       WRITE K-CARDXREF-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-CARDXREF.
       UNLOAD-CARDXREF.
           OPEN INPUT K-CARDXREF
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-CARDXREF NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-CARDXREF-REC TO SEQOUT-REC(1:50)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-CARDXREF.
       LOAD-TCATBALF.
           OPEN OUTPUT K-TCATBALF
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:50) TO K-TCATBALF-REC
                       WRITE K-TCATBALF-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-TCATBALF.
       UNLOAD-TCATBALF.
           OPEN INPUT K-TCATBALF
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-TCATBALF NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-TCATBALF-REC TO SEQOUT-REC(1:50)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-TCATBALF.
       LOAD-DISCGRP.
           OPEN OUTPUT K-DISCGRP
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:50) TO K-DISCGRP-REC
                       WRITE K-DISCGRP-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-DISCGRP.
       UNLOAD-DISCGRP.
           OPEN INPUT K-DISCGRP
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-DISCGRP NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-DISCGRP-REC TO SEQOUT-REC(1:50)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-DISCGRP.
       LOAD-TRANTYPE.
           OPEN OUTPUT K-TRANTYPE
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:60) TO K-TRANTYPE-REC
                       WRITE K-TRANTYPE-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-TRANTYPE.
       UNLOAD-TRANTYPE.
           OPEN INPUT K-TRANTYPE
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-TRANTYPE NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-TRANTYPE-REC TO SEQOUT-REC(1:60)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-TRANTYPE.
       LOAD-TRANCATG.
           OPEN OUTPUT K-TRANCATG
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:60) TO K-TRANCATG-REC
                       WRITE K-TRANCATG-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-TRANCATG.
       UNLOAD-TRANCATG.
           OPEN INPUT K-TRANCATG
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-TRANCATG NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-TRANCATG-REC TO SEQOUT-REC(1:60)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-TRANCATG.
       LOAD-TRANSACT.
           OPEN OUTPUT K-TRANSACT
           PERFORM CHECK-KSDS-OPEN
           PERFORM OPEN-SEQIN
           PERFORM UNTIL WS-EOF = 'Y'
               READ SEQIN
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SEQIN-REC(1:350) TO K-TRANSACT-REC
                       WRITE K-TRANSACT-REC
                       PERFORM COUNT-WRITE
               END-READ
           END-PERFORM
           CLOSE SEQIN K-TRANSACT.
       UNLOAD-TRANSACT.
           OPEN INPUT K-TRANSACT
           PERFORM CHECK-KSDS-OPEN
           OPEN OUTPUT SEQOUT
           PERFORM UNTIL WS-EOF = 'Y'
               READ K-TRANSACT NEXT RECORD
                   AT END MOVE 'Y' TO WS-EOF
                   NOT AT END
                       MOVE SPACES TO SEQOUT-REC
                       MOVE K-TRANSACT-REC TO SEQOUT-REC(1:350)
                       WRITE SEQOUT-REC
                       ADD 1 TO WS-RECORDS
               END-READ
           END-PERFORM
           CLOSE SEQOUT K-TRANSACT.
