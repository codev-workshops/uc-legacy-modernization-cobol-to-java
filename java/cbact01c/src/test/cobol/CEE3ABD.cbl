      *> GnuCOBOL stand-in for the Language Environment abend service:
      *> ends the run unit with the abend code as the return code.
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEE3ABD.
       DATA DIVISION.
       LINKAGE SECTION.
       01  ABCODE   PIC S9(9) BINARY.
       01  TIMING   PIC S9(9) BINARY.
       PROCEDURE DIVISION USING ABCODE TIMING.
           DISPLAY 'CEE3ABD ABCODE=' ABCODE ' TIMING=' TIMING
           STOP RUN RETURNING ABCODE.
