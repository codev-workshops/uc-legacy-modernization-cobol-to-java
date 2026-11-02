      ******************************************************************
      * COBOL port of app/asm/COBDATFT.asm (date format conversion).
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. COBDATFT.
       DATA DIVISION.
       LINKAGE SECTION.
       COPY CODATECN.
       PROCEDURE DIVISION USING CODATECN-REC.
           EVALUATE TRUE
               WHEN YYYYMMDD-IN
                   IF CODATECN-INP-DATE(5:1) = '-'
                      OR YYYYMMDD-OP
                       PERFORM INVALID-INPUT
                   ELSE
                       MOVE CODATECN-INP-DATE(1:4)
                         TO CODATECN-0UT-DATE(1:4)
                       MOVE '-' TO CODATECN-0UT-DATE(5:1)
                       MOVE CODATECN-INP-DATE(5:2)
                         TO CODATECN-0UT-DATE(6:2)
                       MOVE '-' TO CODATECN-0UT-DATE(8:1)
                       MOVE CODATECN-INP-DATE(7:2)
                         TO CODATECN-0UT-DATE(9:2)
                   END-IF
               WHEN YYYY-MM-DD-IN
                   IF YYYY-MM-DD-OP
                       PERFORM INVALID-INPUT
                   ELSE
                       MOVE CODATECN-INP-DATE(1:4)
                         TO CODATECN-0UT-DATE(1:4)
                       MOVE CODATECN-INP-DATE(6:2)
                         TO CODATECN-0UT-DATE(5:2)
                       MOVE CODATECN-INP-DATE(9:2)
                         TO CODATECN-0UT-DATE(7:2)
                   END-IF
               WHEN OTHER
                   PERFORM INVALID-INPUT
           END-EVALUATE
           GOBACK.
       INVALID-INPUT.
           MOVE 'INVALID INPUT' TO CODATECN-ERROR-MSG.
