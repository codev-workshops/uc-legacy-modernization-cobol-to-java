      *> GnuCOBOL stand-in for the HLASM routine app/asm/COBDATFT.asm,
      *> translated instruction by instruction (used only to run the
      *> unmodified CBACT01C as a test oracle).
       IDENTIFICATION DIVISION.
       PROGRAM-ID. COBDATFT.
       DATA DIVISION.
       LINKAGE SECTION.
       01  COREC.
           05  COINTYPE      PIC X.
           05  COINPDT       PIC X(20).
           05  COOUTYPE      PIC X.
           05  COOUTDT       PIC X(20).
           05  COERMSG       PIC X(38).
       PROCEDURE DIVISION USING COREC.
           EVALUATE COINTYPE
             WHEN '1'
               IF COINPDT(5:1) = '-' OR COOUTYPE = '2'
                 MOVE 'INVALID INPUT' TO COERMSG
               ELSE
                 MOVE COINPDT(1:4) TO COOUTDT(1:4)
                 MOVE '-'          TO COOUTDT(5:1)
                 MOVE COINPDT(5:2) TO COOUTDT(6:2)
                 MOVE '-'          TO COOUTDT(8:1)
                 MOVE COINPDT(7:2) TO COOUTDT(9:2)
               END-IF
             WHEN '2'
               IF COOUTYPE = '1'
                 MOVE 'INVALID INPUT' TO COERMSG
               ELSE
                 MOVE COINPDT(1:4)  TO COOUTDT(1:4)
                 MOVE COINPDT(6:2)  TO COOUTDT(5:2)
                 MOVE COINPDT(9:2)  TO COOUTDT(7:2)
               END-IF
             WHEN OTHER
               MOVE 'INVALID INPUT' TO COERMSG
           END-EVALUATE
           MOVE 0 TO RETURN-CODE
           GOBACK.
