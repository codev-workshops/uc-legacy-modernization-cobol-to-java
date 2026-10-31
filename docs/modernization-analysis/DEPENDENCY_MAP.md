# CardDemo Dependency Map

> Call graph, screen navigation, program-to-data CRUD, job-to-dataset I/O and end-to-end data lineage for CardDemo. Built from `EXEC CICS XCTL/LINK/READ/WRITE...`, `CALL`, `EXEC SQL`, `EXEC DLI`/`CBLTDLI`, MQ API calls, `SELECT ... ASSIGN`/`OPEN` modes, JCL DD statements (DISP), IDCAMS control cards, CSD file definitions and scheduler chains. Dynamic targets (`XCTL PROGRAM(CDEMO-TO-PROGRAM)`) are resolved from the program-name literals each program moves into the COMMAREA and from the menu tables COMEN02Y / COADM02Y.

Companion documents: [APPLICATION_INVENTORY.md](APPLICATION_INVENTORY.md) | [DATA_DICTIONARY.md](DATA_DICTIONARY.md) | [HOTSPOT_REPORT.md](HOTSPOT_REPORT.md)

## 1. System Context

```mermaid
flowchart LR
  T[3270 terminal users<br/>regular + admin] -->|CC00 sign-on| CICS
  NET[Card network / POS] -->|MQ request queue| AUTH[COPAUA0C<br/>CP00]
  AUTH -->|MQ reply| NET
  EXT[MQ clients] -->|CDRA / CDRD| MQS[COACCT01 / CODATE01]
  subgraph CICS[CICS region - online]
    UI[25 online programs]
    AUTH
    MQS
  end
  subgraph DATA[Data stores]
    VSAM[(VSAM KSDS<br/>ACCT CARD CUST XREF TRAN USRSEC ...)]
    IMS[(IMS HIDAM<br/>DBPAUTP0)]
    DB2[(DB2 CARDDEMO<br/>TRANSACTION_TYPE / _CATEGORY / AUTHFRDS)]
  end
  UI --> VSAM
  UI --> IMS
  UI --> DB2
  AUTH --> VSAM
  AUTH --> IMS
  MQS --> VSAM
  UI -->|TDQ JOBS -> internal reader| BATCH
  subgraph BATCH[Batch - JCL / Control-M / CA-7]
    POST[POSTTRAN] 
    INT[INTCALC + COMBTRAN]
    RPT[TRANREPT / CREASTMT]
    REF[TRANEXTR / MNTTRDB2]
    PURGE[CBPAUP0J]
  end
  BATCH --> VSAM
  BATCH --> IMS
  BATCH --> DB2
  DAILY[/DALYTRAN.PS daily feed/] --> POST
  RPT --> OUT[/Reports, statements TXT/HTML/PDF/]
```

## 2. Online Call Graph (CICS program flow)

### 2.1 Navigation diagram

```mermaid
flowchart TD
  SGN["COSGN00C<br/>CC00 Sign-on"]
  COPAUA0C["COPAUA0C<br/>CP00 Authorization"]
  COPAUS0C["COPAUS0C<br/>CPVS Authorization"]
  COPAUS1C["COPAUS1C<br/>CPVD Authorization"]
  COPAUS2C["COPAUS2C<br/> Authorization"]
  COTRTLIC["COTRTLIC<br/>CTLI Reference data"]
  COTRTUPC["COTRTUPC<br/>CTTU Reference data"]
  COACCT01["COACCT01<br/>CDRA Account"]
  CODATE01["CODATE01<br/>CDRD Integration"]
  COACTUPC["COACTUPC<br/>CAUP Account"]
  COACTVWC["COACTVWC<br/>CAVW Account"]
  COADM01C["COADM01C<br/>CA00 Navigation"]
  COBIL00C["COBIL00C<br/>CB00 Billing"]
  COCRDLIC["COCRDLIC<br/>CCLI Card"]
  COCRDSLC["COCRDSLC<br/>CCDL Card"]
  COCRDUPC["COCRDUPC<br/>CCUP Card"]
  COMEN01C["COMEN01C<br/>CM00 Navigation"]
  CORPT00C["CORPT00C<br/>CR00 Reporting"]
  COTRN00C["COTRN00C<br/>CT00 Transaction"]
  COTRN01C["COTRN01C<br/>CT01 Transaction"]
  COTRN02C["COTRN02C<br/>CT02 Transaction"]
  COUSR00C["COUSR00C<br/>CU00 Administration"]
  COUSR01C["COUSR01C<br/>CU01 Administration"]
  COUSR02C["COUSR02C<br/>CU02 Administration"]
  COUSR03C["COUSR03C<br/>CU03 Administration"]
  COPAUS1C -.->|LINK| COPAUS2C
  COTRTLIC -->|XCTL| COTRTUPC
  COACTVWC -->|XCTL| COCRDLIC
  COADM01C -->|XCTL| COTRTLIC
  COADM01C -->|XCTL| COTRTUPC
  COADM01C -->|XCTL| COUSR00C
  COADM01C -->|XCTL| COUSR01C
  COADM01C -->|XCTL| COUSR02C
  COADM01C -->|XCTL| COUSR03C
  COCRDLIC -->|XCTL| COCRDSLC
  COCRDLIC -->|XCTL| COCRDUPC
  COCRDSLC -->|XCTL| COCRDLIC
  COMEN01C -->|XCTL| COACTUPC
  COMEN01C -->|XCTL| COACTVWC
  COMEN01C -->|XCTL| COBIL00C
  COMEN01C -->|XCTL| COCRDLIC
  COMEN01C -->|XCTL| COCRDSLC
  COMEN01C -->|XCTL| COCRDUPC
  COMEN01C -->|XCTL| COPAUS0C
  COMEN01C -->|XCTL| CORPT00C
  COMEN01C -->|XCTL| COTRN00C
  COMEN01C -->|XCTL| COTRN01C
  COMEN01C -->|XCTL| COTRN02C
  SGN -->|XCTL| COADM01C
  SGN -->|XCTL| COMEN01C
  COTRN00C -->|XCTL| COTRN01C
  COUSR00C -->|XCTL| COUSR02C
  COUSR00C -->|XCTL| COUSR03C
  CBSTM03A ==>|CALL| CBSTM03B
  CORPT00C ==>|CALL| CSUTLDTC
  COTRN02C ==>|CALL| CSUTLDTC
  MQ[(MQ trigger)] --> COPAUA0C
  MQ --> COACCT01
  MQ --> CODATE01
```

Every function screen returns to its menu (COMEN01C or COADM01C) on PF3 and to COSGN00C on session end; those return edges are omitted from the diagram for readability and listed in 2.2.

### 2.2 Transfer-of-control edges

| From | Tran | Type | To | Tran | Resolution |
|---|---|---|---|---|---|
| CBSTM03A |  | CALL | CBSTM03B |  | static literal |
| COACTUPC | CAUP | XCTL | COCRDLIC | CCLI | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COACTUPC | CAUP | XCTL | COCRDSLC | CCDL | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COACTUPC | CAUP | XCTL | COCRDUPC | CCUP | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COACTUPC | CAUP | XCTL | COMEN01C | CM00 | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COACTVWC | CAVW | XCTL | COCRDLIC | CCLI | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COACTVWC | CAVW | XCTL | COCRDSLC | CCDL | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COACTVWC | CAVW | XCTL | COCRDUPC | CCUP | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COACTVWC | CAVW | XCTL | COMEN01C | CM00 | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COADM01C | CA00 | XCTL | COSGN00C | CC00 | static literal |
| COADM01C | CA00 | XCTL | COTRTLIC | CTLI | menu table |
| COADM01C | CA00 | XCTL | COTRTUPC | CTTU | menu table |
| COADM01C | CA00 | XCTL | COUSR00C | CU00 | menu table |
| COADM01C | CA00 | XCTL | COUSR01C | CU01 | menu table |
| COADM01C | CA00 | XCTL | COUSR02C | CU02 | menu table |
| COADM01C | CA00 | XCTL | COUSR03C | CU03 | menu table |
| COBIL00C | CB00 | XCTL | COMEN01C | CM00 | static literal |
| COBIL00C | CB00 | XCTL | COSGN00C | CC00 | static literal |
| COCRDLIC | CCLI | XCTL | COCRDSLC | CCDL | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COCRDLIC | CCLI | XCTL | COCRDUPC | CCUP | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COCRDLIC | CCLI | XCTL | COMEN01C | CM00 | static literal |
| COCRDSLC | CCDL | XCTL | COCRDLIC | CCLI | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COCRDSLC | CCDL | XCTL | COMEN01C | CM00 | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COCRDUPC | CCUP | XCTL | COCRDLIC | CCLI | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COCRDUPC | CCUP | XCTL | COCRDSLC | CCDL | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COCRDUPC | CCUP | XCTL | COMEN01C | CM00 | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COMEN01C | CM00 | XCTL | COACTUPC | CAUP | menu table |
| COMEN01C | CM00 | XCTL | COACTVWC | CAVW | menu table |
| COMEN01C | CM00 | XCTL | COBIL00C | CB00 | menu table |
| COMEN01C | CM00 | XCTL | COCRDLIC | CCLI | menu table |
| COMEN01C | CM00 | XCTL | COCRDSLC | CCDL | menu table |
| COMEN01C | CM00 | XCTL | COCRDUPC | CCUP | menu table |
| COMEN01C | CM00 | XCTL | COPAUS0C | CPVS | menu table |
| COMEN01C | CM00 | XCTL | CORPT00C | CR00 | menu table |
| COMEN01C | CM00 | XCTL | COSGN00C | CC00 | static literal |
| COMEN01C | CM00 | XCTL | COTRN00C | CT00 | menu table |
| COMEN01C | CM00 | XCTL | COTRN01C | CT01 | menu table |
| COMEN01C | CM00 | XCTL | COTRN02C | CT02 | menu table |
| COPAUS0C | CPVS | XCTL | COSGN00C | CC00 | static literal |
| COPAUS1C | CPVD | XCTL | COPAUS0C | CPVS | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COPAUS1C | CPVD | LINK | COPAUS2C |  | static literal |
| CORPT00C | CR00 | XCTL | COMEN01C | CM00 | static literal |
| CORPT00C | CR00 | XCTL | COSGN00C | CC00 | static literal |
| CORPT00C | CR00 | CALL | CSUTLDTC |  | static literal |
| COSGN00C | CC00 | XCTL | COADM01C | CA00 | static literal |
| COSGN00C | CC00 | XCTL | COMEN01C | CM00 | static literal |
| COTRN00C | CT00 | XCTL | COMEN01C | CM00 | static literal |
| COTRN00C | CT00 | XCTL | COSGN00C | CC00 | static literal |
| COTRN00C | CT00 | XCTL | COTRN01C | CT01 | static literal |
| COTRN01C | CT01 | XCTL | COMEN01C | CM00 | static literal |
| COTRN01C | CT01 | XCTL | COSGN00C | CC00 | static literal |
| COTRN01C | CT01 | XCTL | COTRN00C | CT00 | static literal |
| COTRN02C | CT02 | XCTL | COMEN01C | CM00 | static literal |
| COTRN02C | CT02 | XCTL | COSGN00C | CC00 | static literal |
| COTRN02C | CT02 | CALL | CSUTLDTC |  | static literal |
| COTRTLIC | CTLI | XCTL | COADM01C | CA00 | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COTRTLIC | CTLI | XCTL | COTRTUPC | CTTU | static literal |
| COTRTUPC | CTTU | XCTL | COADM01C | CA00 | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COTRTUPC | CTTU | XCTL | COTRTLIC | CTLI | COMMAREA CDEMO-TO-PROGRAM / CCARD-NEXT-PROG |
| COUSR00C | CU00 | XCTL | COADM01C | CA00 | static literal |
| COUSR00C | CU00 | XCTL | COSGN00C | CC00 | static literal |
| COUSR00C | CU00 | XCTL | COUSR02C | CU02 | static literal |
| COUSR00C | CU00 | XCTL | COUSR03C | CU03 | static literal |
| COUSR01C | CU01 | XCTL | COADM01C | CA00 | static literal |
| COUSR01C | CU01 | XCTL | COSGN00C | CC00 | static literal |
| COUSR02C | CU02 | XCTL | COADM01C | CA00 | static literal |
| COUSR02C | CU02 | XCTL | COSGN00C | CC00 | static literal |
| COUSR03C | CU03 | XCTL | COADM01C | CA00 | static literal |
| COUSR03C | CU03 | XCTL | COSGN00C | CC00 | static literal |

### 2.3 Static CALL graph (batch and utility)

| Caller | Callee | Callee kind |
|---|---|---|
| CBACT01C | CEE3ABD | LE service |
| CBACT01C | COBDATFT | Assembler |
| CBACT02C | CEE3ABD | LE service |
| CBACT03C | CEE3ABD | LE service |
| CBACT04C | CEE3ABD | LE service |
| CBCUS01C | CEE3ABD | LE service |
| CBEXPORT | CEE3ABD | LE service |
| CBIMPORT | CEE3ABD | LE service |
| CBSTM03A | CBSTM03B | COBOL |
| CBSTM03A | CEE3ABD | LE service |
| CBTRN01C | CEE3ABD | LE service |
| CBTRN02C | CEE3ABD | LE service |
| CBTRN03C | CEE3ABD | LE service |
| COACCT01 | MQCLOSE | MQ API |
| COACCT01 | MQGET | MQ API |
| COACCT01 | MQOPEN | MQ API |
| COACCT01 | MQPUT | MQ API |
| COBSWAIT | MVSWAIT | Assembler |
| CODATE01 | MQCLOSE | MQ API |
| CODATE01 | MQGET | MQ API |
| CODATE01 | MQOPEN | MQ API |
| CODATE01 | MQPUT | MQ API |
| COPAUA0C | MQCLOSE | MQ API |
| COPAUA0C | MQGET | MQ API |
| COPAUA0C | MQOPEN | MQ API |
| COPAUA0C | MQPUT1 | MQ API |
| CORPT00C | CSUTLDTC | COBOL |
| COTRN02C | CSUTLDTC | COBOL |
| CSUTLDTC | CEEDAYS | LE service |
| DBUNLDGS | CBLTDLI | IMS DL/I API |
| PAUDBLOD | CBLTDLI | IMS DL/I API |
| PAUDBUNL | CBLTDLI | IMS DL/I API |

```mermaid
flowchart LR
  CBSTM03A --> CBSTM03B
  CBACT01C --> COBDATFT[[COBDATFT asm]]
  COBSWAIT --> MVSWAIT[[MVSWAIT asm]]
  CORPT00C --> CSUTLDTC
  COTRN02C --> CSUTLDTC
  CSUTLDTC --> CEEDAYS[[LE CEEDAYS]]
  COPAUS1C -.LINK.-> COPAUS2C
  PAUDBLOD --> CBLTDLI[[IMS CBLTDLI]]
  PAUDBUNL --> CBLTDLI
  DBUNLDGS --> CBLTDLI
  COPAUA0C --> MQAPI[[MQ API]]
  COACCT01 --> MQAPI
  CODATE01 --> MQAPI
```

All batch programs also call `CEE3ABD` (LE abend) on unrecoverable I/O errors.

## 3. Program -> Copybook Dependencies

Entity copybooks (X = COPY). Changing a column copybook impacts every marked program.

| Program | CVACT01Y | CVACT02Y | CVACT03Y | CVCUS01Y | CVTRA01Y | CVTRA02Y | CVTRA03Y | CVTRA04Y | CVTRA05Y | CVTRA06Y | CSUSR01Y | COCOM01Y | CIPAUSMY | CIPAUDTY | Other copybooks |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| CBACT01C | X |  |  |  |  |  |  |  |  |  |  |  |  |  | CODATECN |
| CBACT02C |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |
| CBACT03C |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |
| CBACT04C | X |  | X |  | X | X |  |  | X |  |  |  |  |  |  |
| CBCUS01C |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |
| CBEXPORT | X | X | X | X |  |  |  |  | X |  |  |  |  |  | CVEXPORT |
| CBIMPORT | X | X | X | X |  |  |  |  | X |  |  |  |  |  | CVEXPORT |
| CBPAUP0C |  |  |  |  |  |  |  |  |  |  |  |  | X | X |  |
| CBSTM03A | X |  | X |  |  |  |  |  |  |  |  |  |  |  | COSTM01, CUSTREC |
| CBSTM03B |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| CBTRN01C | X | X | X | X |  |  |  |  | X | X |  |  |  |  |  |
| CBTRN02C | X |  | X |  | X |  |  |  | X | X |  |  |  |  |  |
| CBTRN03C |  |  | X |  |  |  | X | X | X |  |  |  |  |  | CVTRA07Y |
| COACCT01 | X |  |  |  |  |  |  |  |  |  |  |  |  |  | CMQGMOV, CMQMDV, CMQODV, CMQPMOV, CMQTML, CMQV, PERFORM, REPLACING |
| COACTUPC | X |  | X | X |  |  |  |  |  |  | X | X |  |  | COACTUP, COTTL01Y, CSDAT01Y, CSLKPCDY, CSMSG01Y, CSMSG02Y, CSSETATY, CSSTRPFY, CSUTLDPY, CSUTLDWY, CVCRD01Y, DFHAID, DFHBMSCA |
| COACTVWC | X | X | X | X |  |  |  |  |  |  | X | X |  |  | COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSSTRPFY, CVCRD01Y, DFHAID, DFHBMSCA |
| COADM01C |  |  |  |  |  |  |  |  |  |  | X | X |  |  | COADM01, COADM02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COBIL00C | X |  | X |  |  |  |  |  | X |  |  | X |  |  | COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COBSWAIT |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| COBTUPDT |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| COCRDLIC |  | X |  |  |  |  |  |  |  |  | X | X |  |  | COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSSTRPFY, CVCRD01Y, DFHAID, DFHBMSCA |
| COCRDSLC |  | X |  | X |  |  |  |  |  |  | X | X |  |  | COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSSTRPFY, CVCRD01Y, DFHAID, DFHBMSCA |
| COCRDUPC |  | X |  | X |  |  |  |  |  |  | X | X |  |  | COCRDUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSSTRPFY, CVCRD01Y, DFHAID, DFHBMSCA |
| CODATE01 |  |  |  |  |  |  |  |  |  |  |  |  |  |  | CMQGMOV, CMQMDV, CMQODV, CMQPMOV, CMQTML, CMQV, PERFORM, REPLACING |
| COMEN01C |  |  |  |  |  |  |  |  |  |  | X | X |  |  | COMEN01, COMEN02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COPAUA0C | X |  | X | X |  |  |  |  |  |  |  |  | X | X | CCPAUERY, CCPAURLY, CCPAURQY, CMQGMOV, CMQMDV, CMQODV, CMQPMOV, CMQTML, CMQV |
| COPAUS0C | X | X | X | X |  |  |  |  |  |  |  | X | X | X | COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA |
| COPAUS1C |  |  |  |  |  |  |  |  |  |  |  | X | X | X | COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA |
| COPAUS2C |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  |
| CORPT00C |  |  |  |  |  |  |  |  | X |  |  | X |  |  | CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COSGN00C |  |  |  |  |  |  |  |  |  |  | X | X |  |  | COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COTRN00C |  |  |  |  |  |  |  |  | X |  |  | X |  |  | COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COTRN01C |  |  |  |  |  |  |  |  | X |  |  | X |  |  | COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COTRN02C | X |  | X |  |  |  |  |  | X |  |  | X |  |  | COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COTRTLIC |  | X |  |  |  |  |  |  |  |  | X | X |  |  | COTRTLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSSTRPFY, CVCRD01Y, DFHAID, DFHBMSCA |
| COTRTUPC |  |  |  |  |  |  |  |  |  |  | X | X |  |  | COTRTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSSETATY, CSSTRPFY, CSUTLDWY, CVCRD01Y, DFHAID, DFHBMSCA |
| COUSR00C |  |  |  |  |  |  |  |  |  |  | X | X |  |  | COTTL01Y, COUSR00, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COUSR01C |  |  |  |  |  |  |  |  |  |  | X | X |  |  | COTTL01Y, COUSR01, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COUSR02C |  |  |  |  |  |  |  |  |  |  | X | X |  |  | COTTL01Y, COUSR02, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COUSR03C |  |  |  |  |  |  |  |  |  |  | X | X |  |  | COTTL01Y, COUSR03, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| CSUTLDTC |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| DBUNLDGS |  |  |  |  |  |  |  |  |  |  |  |  | X | X | IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB |
| PAUDBLOD |  |  |  |  |  |  |  |  |  |  |  |  | X | X | IMSFUNCS, PAUTBPCB |
| PAUDBUNL |  |  |  |  |  |  |  |  |  |  |  |  | X | X | IMSFUNCS, PAUTBPCB |

**Highest fan-in copybooks**: COCOM01Y (21), COTTL01Y (21), CSDAT01Y (21), CSMSG01Y (21), DFHAID (21), DFHBMSCA (21), CVACT01Y (14), CVACT03Y (14), CSUSR01Y (14), CVTRA05Y (11), CVCUS01Y (10), CVACT02Y (10)

## 4. Online Program -> Data Store CRUD Matrix

C = create/insert, R = read/browse, U = update/rewrite, D = delete, L = READ UPDATE lock without rewrite.

| Program | Tran | ACCTDAT | CUSTDAT | CARDDAT | CARDAIX | CCXREF | CXACAIX | TRANSACT | USRSEC | IMS PAUTDB | DB2 TRANSACTION_TYPE | DB2 AUTHFRDS | MQ | TDQ |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| COPAUA0C | CP00 | R | R |  |  | R |  |  |  | CRU |  |  | CR | CSSL |
| COPAUS0C | CPVS | R | R |  |  |  | R |  |  | R |  |  |  |  |
| COPAUS1C | CPVD |  |  |  |  |  |  |  |  | RU |  |  |  |  |
| COPAUS2C |  |  |  |  |  |  |  |  |  |  |  | CU |  |  |
| COTRTLIC | CTLI |  |  |  |  |  |  |  |  |  | RUD |  |  |  |
| COTRTUPC | CTTU |  |  |  |  |  |  |  |  |  | CRUD |  |  |  |
| COACCT01 | CDRA | R |  |  |  |  |  |  |  |  |  |  | CR |  |
| CODATE01 | CDRD |  |  |  |  |  |  |  |  |  |  |  | CR |  |
| COACTUPC | CAUP | RU | RU |  |  |  | R |  |  |  |  |  |  |  |
| COACTVWC | CAVW | R | R |  |  |  | R |  |  |  |  |  |  |  |
| COADM01C | CA00 |  |  |  |  |  |  |  |  |  |  |  |  |  |
| COBIL00C | CB00 | RU |  |  |  |  | R | CR |  |  |  |  |  |  |
| COCRDLIC | CCLI |  |  | R |  |  |  |  |  |  |  |  |  |  |
| COCRDSLC | CCDL |  |  | R | R |  |  |  |  |  |  |  |  |  |
| COCRDUPC | CCUP |  |  | RU |  |  |  |  |  |  |  |  |  |  |
| COMEN01C | CM00 |  |  |  |  |  |  |  |  |  |  |  |  |  |
| CORPT00C | CR00 |  |  |  |  |  |  |  |  |  |  |  |  | JOBS |
| COSGN00C | CC00 |  |  |  |  |  |  |  | R |  |  |  |  |  |
| COTRN00C | CT00 |  |  |  |  |  |  | R |  |  |  |  |  |  |
| COTRN01C | CT01 |  |  |  |  |  |  | RL |  |  |  |  |  |  |
| COTRN02C | CT02 |  |  |  |  | R | R | CR |  |  |  |  |  |  |
| COUSR00C | CU00 |  |  |  |  |  |  |  | R |  |  |  |  |  |
| COUSR01C | CU01 |  |  |  |  |  |  |  | C |  |  |  |  |  |
| COUSR02C | CU02 |  |  |  |  |  |  |  | RU |  |  |  |  |  |
| COUSR03C | CU03 |  |  |  |  |  |  |  | RD |  |  |  |  |  |

| CICS file | Dataset |
|---|---|
| ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS |
| CARDAIX | AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH |
| CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS |
| CCXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS |
| CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS |
| CXACAIX | AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS |

## 5. Batch Job -> Dataset I/O

Dataset names are shown without the `AWS.M2.CARDDEMO.` high-level qualifier; GDG relative generations are folded into the base. "Define/Delete" covers IDCAMS DEFINE/DELETE and IEFBR14 DISP=(MOD,DELETE) clean-up.

| Job | Category | Programs | Reads | Writes / updates | Defines / deletes |
|---|---|---|---|---|---|
| **ACCTFILE** | Data load (VSAM provisioning) | IDCAMS | ACCTDATA.PS | ACCTDATA.VSAM.KSDS | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | Data load (VSAM provisioning) | SDSF, IDCAMS | CARDDATA.PS<br/>CARDDATA.VSAM.KSDS | CARDDATA.VSAM.AIX<br/>CARDDATA.VSAM.KSDS | CARDDATA.VSAM.AIX<br/>CARDDATA.VSAM.KSDS |
| **CBEXPORT** | Data migration | IDCAMS, CBEXPORT | ACCTDATA.VSAM.KSDS<br/>CARDDATA.VSAM.KSDS<br/>CARDXREF.VSAM.KSDS<br/>CUSTDATA.VSAM.KSDS<br/>TRANSACT.VSAM.KSDS | EXPORT.DATA | EXPORT.DATA |
| **CBIMPORT** | Data migration | CBIMPORT | EXPORT.DATA | ACCTDATA.IMPORT<br/>CARDXREF.IMPORT<br/>CUSTDATA.IMPORT<br/>IMPORT.ERRORS<br/>TRANSACT.IMPORT |  |
| **CBPAUP0J** | Business batch (IMS) | DFSRRC00 (BMP CBPAUP0C) | IMS PAUTDB | IMS PAUTDB |  |
| **COMBTRAN** | Business batch | SORT, IDCAMS | SYSTRAN<br/>TRANSACT.BKUP<br/>TRANSACT.COMBINED | TRANSACT.COMBINED<br/>TRANSACT.VSAM.KSDS |  |
| **CREADB2** | DB2 setup | IKJEFT01, IEFBR14 | &LBNM..CNTL(DB2CREAT)<br/>&LBNM..CNTL(DB2LTCAT)<br/>&LBNM..CNTL(DB2LTTYP) |  |  |
| **CREASTMT** | Reporting | IDCAMS, SORT, IEFBR14, CBSTM03A | ACCTDATA.VSAM.KSDS<br/>CARDXREF.VSAM.KSDS<br/>CUSTDATA.VSAM.KSDS<br/>TRANSACT.VSAM.KSDS<br/>TRXFL.SEQ<br/>TRXFL.VSAM.KSDS | STATEMNT.HTML<br/>TRXFL.SEQ<br/>TRXFL.VSAM.KSDS | STATEMNT.HTML<br/>STATEMNT.PS<br/>TRXFL.SEQ<br/>TRXFL.VSAM.KSDS |
| **CUSTFILE** | Data load (VSAM provisioning) | SDSF, IDCAMS | CUSTDATA.PS | CUSTDATA.VSAM.KSDS | CUSTDATA.VSAM.KSDS |
| **DBPAUTP0** | IMS database utility | IEFBR14, DFSRRC00 | IMS PAUTDB | IMSDATA.DBPAUTP0 | IMSDATA.DBPAUTP0 |
| **DEFCUST** | Data load (VSAM provisioning) | IDCAMS |  |  | AWS.CCDA.CUSTDATA.CLUSTER<br/>AWS.CUSTDATA.CLUSTER |
| **DEFGDGD** | Data load (VSAM provisioning) | IDCAMS, IEBGENER | DISCGRP.PS<br/>TRANCATG.PS<br/>TRANTYPE.PS | DISCGRP.BKUP<br/>TRANCATG.PS.BKUP<br/>TRANTYPE.BKUP |  |
| **DISCGRP** | Data load (VSAM provisioning) | IDCAMS | DISCGRP.PS | DISCGRP.VSAM.KSDS | DISCGRP.VSAM.KSDS |
| **DUSRSECJ** | Data load (VSAM provisioning) | IEFBR14, IEBGENER, IDCAMS | USRSEC.PS | USRSEC.PS<br/>USRSEC.VSAM.KSDS | USRSEC.PS<br/>USRSEC.VSAM.KSDS |
| **ESDSRRDS** | Data load (VSAM provisioning) | IEFBR14, IEBGENER, IDCAMS | ESDSRRDS.PS | ESDSRRDS.PS<br/>USRSEC.VSAM.ESDS<br/>USRSEC.VSAM.RRDS | ESDSRRDS.PS<br/>USRSEC.VSAM.ESDS<br/>USRSEC.VSAM.RRDS |
| **INTCALC** | Business batch | CBACT04C | CARDXREF.VSAM.AIX.PATH<br/>CARDXREF.VSAM.KSDS<br/>DISCGRP.VSAM.KSDS<br/>TCATBALF.VSAM.KSDS | ACCTDATA.VSAM.KSDS<br/>SYSTRAN |  |
| **INTRDRJ1** | Utility / integration | IDCAMS, IEBGENER | AWS.M2.CARDEMO.FTP.TEST<br/>JCL(INTRDRJ2) | AWS.M2.CARDEMO.FTP.TEST.BKUP |  |
| **INTRDRJ2** | Utility / integration | IDCAMS | AWS.M2.CARDEMO.FTP.TEST.BKUP | AWS.M2.CARDEMO.FTP.TEST.BKUP.INTRDR |  |
| **LOADPADB** | IMS database utility | DFSRRC00 (BMP PAUDBLOD) | PAUTDB.CHILD.FILEO<br/>PAUTDB.ROOT.FILEO | IMS PAUTDB |  |
| **MNTTRDB2** | Business batch (DB2) | IKJEFT01 (DSN RUN COBTUPDT) | INPFILE | DB2 TRANSACTION_TYPE |  |
| **POSTTRAN** | Business batch | CBTRN02C | CARDXREF.VSAM.KSDS<br/>DALYTRAN.PS | ACCTDATA.VSAM.KSDS<br/>DALYREJS<br/>TCATBALF.VSAM.KSDS<br/>TRANSACT.VSAM.KSDS |  |
| **PRTCATBL** | Reporting | IEFBR14, PROC REPROC, SORT | TCATBALF.BKUP<br/>TCATBALF.VSAM.KSDS | TCATBALF.BKUP<br/>TCATBALF.REPT | TCATBALF.REPT |
| **READACCT** | Utility / diagnostics | IEFBR14, CBACT01C | ACCTDATA.VSAM.KSDS | ACCTDATA.ARRYPS<br/>ACCTDATA.PSCOMP<br/>ACCTDATA.VBPS | ACCTDATA.ARRYPS<br/>ACCTDATA.PSCOMP<br/>ACCTDATA.VBPS |
| **READCARD** | Utility / diagnostics | CBACT02C | CARDDATA.VSAM.KSDS |  |  |
| **READCUST** | Utility / diagnostics | CBCUS01C | CUSTDATA.VSAM.KSDS |  |  |
| **READXREF** | Utility / diagnostics | CBACT03C | CARDXREF.VSAM.KSDS |  |  |
| **TCATBALF** | Data load (VSAM provisioning) | IDCAMS | TCATBALF.PS | TCATBALF.VSAM.KSDS | TCATBALF.VSAM.KSDS |
| **TRANBKP** | Business batch | PROC REPROC, IDCAMS | TRANSACT.VSAM.KSDS | TRANSACT.BKUP | TRANSACT.VSAM.AIX<br/>TRANSACT.VSAM.KSDS |
| **TRANCATG** | Data load (VSAM provisioning) | IDCAMS | TRANCATG.PS | TRANCATG.VSAM.KSDS | TRANCATG.VSAM.KSDS |
| **TRANEXTR** | DB2 extract | IEBGENER, IEFBR14, IKJEFT01 (DSN RUN DSNTIAUL) | TRANCATG.PS<br/>TRANTYPE.PS | TRANCATG.PS<br/>TRANCATG.PS.BKUP<br/>TRANTYPE.BKUP<br/>TRANTYPE.PS | TRANCATG.PS<br/>TRANTYPE.PS |
| **TRANFILE** | Data load (VSAM provisioning) | SDSF, IDCAMS | DALYTRAN.PS.INIT<br/>TRANSACT.VSAM.KSDS | TRANSACT.VSAM.AIX<br/>TRANSACT.VSAM.KSDS | TRANSACT.VSAM.AIX<br/>TRANSACT.VSAM.KSDS |
| **TRANIDX** | Data load (VSAM provisioning) | IDCAMS | TRANSACT.VSAM.KSDS | TRANSACT.VSAM.AIX | TRANSACT.VSAM.AIX |
| **TRANREPT** | Reporting | PROC REPROC, SORT, CBTRN03C | CARDXREF.VSAM.KSDS<br/>DATEPARM<br/>TRANCATG.VSAM.KSDS<br/>TRANSACT.BKUP<br/>TRANSACT.DALY<br/>TRANSACT.VSAM.KSDS<br/>TRANTYPE.VSAM.KSDS | TRANREPT<br/>TRANSACT.BKUP<br/>TRANSACT.DALY |  |
| **TRANTYPE** | Data load (VSAM provisioning) | IDCAMS | TRANTYPE.PS | TRANTYPE.VSAM.KSDS | TRANTYPE.VSAM.KSDS |
| **TXT2PDF1** | Reporting | IKJEFT1B | STATEMNT.PS |  |  |
| **UNLDGSAM** | IMS database utility | DFSRRC00 (DLI DBUNLDGS) | IMS PAUTDB | PAUTDB.CHILD.GSAM<br/>PAUTDB.ROOT.GSAM |  |
| **UNLDPADB** | IMS database utility | IEFBR14, DFSRRC00 (DLI PAUDBUNL) | IMS PAUTDB | PAUTDB.CHILD.FILEO<br/>PAUTDB.ROOT.FILEO | PAUTDB.CHILD.FILEO<br/>PAUTDB.ROOT.FILEO |
| **XREFFILE** | Data load (VSAM provisioning) | IDCAMS | CARDXREF.PS<br/>CARDXREF.VSAM.KSDS | CARDXREF.VSAM.AIX<br/>CARDXREF.VSAM.KSDS | CARDXREF.VSAM.AIX<br/>CARDXREF.VSAM.KSDS |

### 5.1 Step-level detail for business batch

| Job | Step | Program / utility | Dataset | Access |
|---|---|---|---|---|
| CBPAUP0J | STEP01 | CBPAUP0C | IMS PAUTDB | RD |
| CREADB2 | CRCRDDB | IKJEFT01 | &LBNM..CNTL(DB2CREAT) | R |
| CREADB2 | RUNTEP2 | IKJEFT01 | &LBNM..CNTL(DB2LTTYP) | R |
| CREADB2 | LDTCCAT | IKJEFT01 | &LBNM..CNTL(DB2LTCAT) | R |
| MNTTRDB2 | STEP1 | COBTUPDT | DB2 TRANSACTION_TYPE | CUD |
| MNTTRDB2 | STEP1 | COBTUPDT | INPFILE | R |
| TRANEXTR | STEP10 | IEBGENER | TRANTYPE.PS | R |
| TRANEXTR | STEP10 | IEBGENER | TRANTYPE.BKUP | W |
| TRANEXTR | STEP20 | IEBGENER | TRANCATG.PS | R |
| TRANEXTR | STEP20 | IEBGENER | TRANCATG.PS.BKUP | W |
| TRANEXTR | STEP30 | IEFBR14 | TRANTYPE.PS | Delete |
| TRANEXTR | STEP30 | IEFBR14 | TRANCATG.PS | Delete |
| TRANEXTR | STEP40 | IKJEFT01 | TRANTYPE.PS | W |
| TRANEXTR | STEP50 | IKJEFT01 | TRANCATG.PS | W |
| CBEXPORT | STEP01 | IDCAMS | EXPORT.DATA | Define |
| CBEXPORT | STEP01 | IDCAMS | EXPORT.DATA | Delete |
| CBEXPORT | STEP02 | CBEXPORT | CUSTDATA.VSAM.KSDS | R |
| CBEXPORT | STEP02 | CBEXPORT | ACCTDATA.VSAM.KSDS | R |
| CBEXPORT | STEP02 | CBEXPORT | CARDXREF.VSAM.KSDS | R |
| CBEXPORT | STEP02 | CBEXPORT | TRANSACT.VSAM.KSDS | R |
| CBEXPORT | STEP02 | CBEXPORT | CARDDATA.VSAM.KSDS | R |
| CBEXPORT | STEP02 | CBEXPORT | EXPORT.DATA | W |
| CBIMPORT | STEP01 | CBIMPORT | EXPORT.DATA | R |
| CBIMPORT | STEP01 | CBIMPORT | CUSTDATA.IMPORT | W |
| CBIMPORT | STEP01 | CBIMPORT | ACCTDATA.IMPORT | W |
| CBIMPORT | STEP01 | CBIMPORT | CARDXREF.IMPORT | W |
| CBIMPORT | STEP01 | CBIMPORT | TRANSACT.IMPORT | W |
| CBIMPORT | STEP01 | CBIMPORT | IMPORT.ERRORS | W |
| COMBTRAN | STEP05R | SORT | TRANSACT.BKUP | R |
| COMBTRAN | STEP05R | SORT | SYSTRAN | R |
| COMBTRAN | STEP05R | SORT | TRANSACT.COMBINED | W |
| COMBTRAN | STEP10 | IDCAMS REPRO | TRANSACT.COMBINED | R |
| COMBTRAN | STEP10 | IDCAMS REPRO | TRANSACT.VSAM.KSDS | W |
| CREASTMT | DELDEF01 | IDCAMS | TRXFL.VSAM.KSDS | Define |
| CREASTMT | DELDEF01 | IDCAMS | TRXFL.SEQ | Delete |
| CREASTMT | DELDEF01 | IDCAMS | TRXFL.VSAM.KSDS | Delete |
| CREASTMT | STEP010 | SORT | TRANSACT.VSAM.KSDS | R |
| CREASTMT | STEP010 | SORT | TRXFL.SEQ | W |
| CREASTMT | STEP020 | IDCAMS REPRO | TRXFL.SEQ | R |
| CREASTMT | STEP020 | IDCAMS REPRO | TRXFL.VSAM.KSDS | W |
| CREASTMT | STEP030 | IEFBR14 | STATEMNT.HTML | Delete |
| CREASTMT | STEP030 | IEFBR14 | STATEMNT.PS | Delete |
| CREASTMT | STEP040 | CBSTM03A | TRXFL.VSAM.KSDS | R |
| CREASTMT | STEP040 | CBSTM03A | CARDXREF.VSAM.KSDS | R |
| CREASTMT | STEP040 | CBSTM03A | ACCTDATA.VSAM.KSDS | R |
| CREASTMT | STEP040 | CBSTM03A | CUSTDATA.VSAM.KSDS | R |
| CREASTMT | STEP040 | CBSTM03A | STATEMNT.HTML | W |
| INTCALC | STEP15 | CBACT04C | TCATBALF.VSAM.KSDS | R |
| INTCALC | STEP15 | CBACT04C | CARDXREF.VSAM.KSDS | R |
| INTCALC | STEP15 | CBACT04C | CARDXREF.VSAM.AIX.PATH | R |
| INTCALC | STEP15 | CBACT04C | ACCTDATA.VSAM.KSDS | RW |
| INTCALC | STEP15 | CBACT04C | DISCGRP.VSAM.KSDS | R |
| INTCALC | STEP15 | CBACT04C | SYSTRAN | W |
| POSTTRAN | STEP15 | CBTRN02C | TRANSACT.VSAM.KSDS | W |
| POSTTRAN | STEP15 | CBTRN02C | DALYTRAN.PS | R |
| POSTTRAN | STEP15 | CBTRN02C | CARDXREF.VSAM.KSDS | R |
| POSTTRAN | STEP15 | CBTRN02C | DALYREJS | W |
| POSTTRAN | STEP15 | CBTRN02C | ACCTDATA.VSAM.KSDS | RW |
| POSTTRAN | STEP15 | CBTRN02C | TCATBALF.VSAM.KSDS | RW |
| PRTCATBL | DELDEF | IEFBR14 | TCATBALF.REPT | Delete |
| PRTCATBL | STEP05R |  | TCATBALF.VSAM.KSDS | R |
| PRTCATBL | STEP05R |  | TCATBALF.BKUP | W |
| PRTCATBL | STEP05R | REPROC | TCATBALF.VSAM.KSDS | R |
| PRTCATBL | STEP05R | REPROC | TCATBALF.BKUP | W |
| PRTCATBL | STEP10R | SORT | TCATBALF.BKUP | R |
| PRTCATBL | STEP10R | SORT | TCATBALF.REPT | W |
| TRANBKP | STEP05R |  | TRANSACT.VSAM.KSDS | R |
| TRANBKP | STEP05R |  | TRANSACT.BKUP | W |
| TRANBKP | STEP05R | REPROC | TRANSACT.VSAM.KSDS | R |
| TRANBKP | STEP05R | REPROC | TRANSACT.BKUP | W |
| TRANBKP | STEP05 | IDCAMS | TRANSACT.VSAM.KSDS | Delete |
| TRANBKP | STEP05 | IDCAMS | TRANSACT.VSAM.AIX | Delete |
| TRANBKP | STEP10 | IDCAMS | TRANSACT.VSAM.KSDS | Define |
| TRANREPT | STEP05R |  | TRANSACT.VSAM.KSDS | R |
| TRANREPT | STEP05R |  | TRANSACT.BKUP | W |
| TRANREPT | STEP05R | REPROC | TRANSACT.VSAM.KSDS | R |
| TRANREPT | STEP05R | REPROC | TRANSACT.BKUP | W |
| TRANREPT | STEP05R | SORT | TRANSACT.BKUP | R |
| TRANREPT | STEP05R | SORT | TRANSACT.DALY | W |
| TRANREPT | STEP10R | CBTRN03C | TRANSACT.DALY | R |
| TRANREPT | STEP10R | CBTRN03C | CARDXREF.VSAM.KSDS | R |
| TRANREPT | STEP10R | CBTRN03C | TRANTYPE.VSAM.KSDS | R |
| TRANREPT | STEP10R | CBTRN03C | TRANCATG.VSAM.KSDS | R |
| TRANREPT | STEP10R | CBTRN03C | DATEPARM | R |
| TRANREPT | STEP10R | CBTRN03C | TRANREPT | W |
| TXT2PDF1 | TXT2PDF | IKJEFT1B | STATEMNT.PS | R |

## 6. Data Lineage by Dataset (producers and consumers)

| Dataset / store | Written by jobs | Written by online programs | Read by jobs | Read by online programs |
|---|---|---|---|---|
| `ACCTDATA.VSAM.KSDS` | ACCTFILE, INTCALC, POSTTRAN | COACTUPC, COBIL00C | CBEXPORT, CREASTMT, INTCALC, POSTTRAN, READACCT | COACCT01, COACTUPC, COACTVWC, COBIL00C, COPAUA0C, COPAUS0C |
| `TRANSACT.VSAM.KSDS` | COMBTRAN, POSTTRAN, TRANFILE | COBIL00C, COTRN02C | CBEXPORT, CREASTMT, TRANBKP, TRANFILE, TRANIDX, TRANREPT | COBIL00C, COTRN00C, COTRN01C, COTRN02C |
| `CARDXREF.VSAM.KSDS` | XREFFILE |  | CBEXPORT, CREASTMT, INTCALC, POSTTRAN, READXREF, TRANREPT, XREFFILE | COACTUPC, COACTVWC, COBIL00C, COPAUA0C, COPAUS0C, COTRN02C |
| `IMS PAUTDB` | CBPAUP0J, LOADPADB | COPAUA0C, COPAUS1C | CBPAUP0J, DBPAUTP0, UNLDGSAM, UNLDPADB | COPAUA0C, COPAUS0C, COPAUS1C |
| `CUSTDATA.VSAM.KSDS` | CUSTFILE | COACTUPC | CBEXPORT, CREASTMT, READCUST | COACTUPC, COACTVWC, COPAUA0C, COPAUS0C |
| `CARDDATA.VSAM.KSDS` | CARDFILE | COCRDUPC | CARDFILE, CBEXPORT, READCARD | COCRDLIC, COCRDSLC, COCRDUPC |
| `USRSEC.VSAM.KSDS` | DUSRSECJ | COUSR01C, COUSR02C, COUSR03C |  | COSGN00C, COUSR00C, COUSR02C, COUSR03C |
| `DB2 TRANSACTION_TYPE` | MNTTRDB2 | COTRTLIC, COTRTUPC |  | COTRTLIC, COTRTUPC |
| `TCATBALF.VSAM.KSDS` | POSTTRAN, TCATBALF |  | INTCALC, POSTTRAN, PRTCATBL |  |
| `TRANCATG.PS` | TRANEXTR |  | DEFGDGD, TRANCATG, TRANEXTR |  |
| `TRANSACT.BKUP` | TRANBKP, TRANREPT |  | COMBTRAN, TRANREPT |  |
| `TRANTYPE.PS` | TRANEXTR |  | DEFGDGD, TRANEXTR, TRANTYPE |  |
| `AWS.M2.CARDEMO.FTP.TEST.BKUP` | INTRDRJ1 |  | INTRDRJ2 |  |
| `DISCGRP.PS` |  |  | DEFGDGD, DISCGRP |  |
| `DISCGRP.VSAM.KSDS` | DISCGRP |  | INTCALC |  |
| `ESDSRRDS.PS` | ESDSRRDS |  | ESDSRRDS |  |
| `EXPORT.DATA` | CBEXPORT |  | CBIMPORT |  |
| `PAUTDB.CHILD.FILEO` | UNLDPADB |  | LOADPADB |  |
| `PAUTDB.ROOT.FILEO` | UNLDPADB |  | LOADPADB |  |
| `SYSTRAN` | INTCALC |  | COMBTRAN |  |
| `TCATBALF.BKUP` | PRTCATBL |  | PRTCATBL |  |
| `TRANCATG.PS.BKUP` | DEFGDGD, TRANEXTR |  |  |  |
| `TRANCATG.VSAM.KSDS` | TRANCATG |  | TRANREPT |  |
| `TRANSACT.COMBINED` | COMBTRAN |  | COMBTRAN |  |
| `TRANSACT.DALY` | TRANREPT |  | TRANREPT |  |
| `TRANSACT.VSAM.AIX` | TRANFILE, TRANIDX |  |  |  |
| `TRANTYPE.BKUP` | DEFGDGD, TRANEXTR |  |  |  |
| `TRANTYPE.VSAM.KSDS` | TRANTYPE |  | TRANREPT |  |
| `TRXFL.SEQ` | CREASTMT |  | CREASTMT |  |
| `TRXFL.VSAM.KSDS` | CREASTMT |  | CREASTMT |  |
| `USRSEC.PS` | DUSRSECJ |  | DUSRSECJ |  |
| `ACCTDATA.ARRYPS` | READACCT |  |  |  |
| `ACCTDATA.IMPORT` | CBIMPORT |  |  |  |
| `ACCTDATA.PS` |  |  | ACCTFILE |  |
| `ACCTDATA.PSCOMP` | READACCT |  |  |  |
| `ACCTDATA.VBPS` | READACCT |  |  |  |
| `AWS.M2.CARDEMO.FTP.TEST` |  |  | INTRDRJ1 |  |
| `AWS.M2.CARDEMO.FTP.TEST.BKUP.INTRDR` | INTRDRJ2 |  |  |  |
| `CARDDATA.PS` |  |  | CARDFILE |  |
| `CARDDATA.VSAM.AIX` | CARDFILE |  |  |  |
| `CARDXREF.IMPORT` | CBIMPORT |  |  |  |
| `CARDXREF.PS` |  |  | XREFFILE |  |
| `CARDXREF.VSAM.AIX` | XREFFILE |  |  |  |
| `CARDXREF.VSAM.AIX.PATH` |  |  | INTCALC |  |
| `CUSTDATA.IMPORT` | CBIMPORT |  |  |  |
| `CUSTDATA.PS` |  |  | CUSTFILE |  |
| `DALYREJS` | POSTTRAN |  |  |  |
| `DALYTRAN.PS` |  |  | POSTTRAN |  |
| `DALYTRAN.PS.INIT` |  |  | TRANFILE |  |
| `DATEPARM` |  |  | TRANREPT |  |
| `DB2 AUTHFRDS` |  | COPAUS2C |  |  |
| `DISCGRP.BKUP` | DEFGDGD |  |  |  |
| `IMPORT.ERRORS` | CBIMPORT |  |  |  |
| `IMSDATA.DBPAUTP0` | DBPAUTP0 |  |  |  |
| `INPFILE` |  |  | MNTTRDB2 |  |
| `JCL(INTRDRJ2)` |  |  | INTRDRJ1 |  |
| `PAUTDB.CHILD.GSAM` | UNLDGSAM |  |  |  |
| `PAUTDB.ROOT.GSAM` | UNLDGSAM |  |  |  |
| `STATEMNT.HTML` | CREASTMT |  |  |  |
| `STATEMNT.PS` |  |  | TXT2PDF1 |  |
| `TCATBALF.PS` |  |  | TCATBALF |  |
| `TCATBALF.REPT` | PRTCATBL |  |  |  |
| `TRANREPT` | TRANREPT |  |  |  |
| `TRANSACT.IMPORT` | CBIMPORT |  |  |  |
| `USRSEC.VSAM.ESDS` | ESDSRRDS |  |  |  |
| `USRSEC.VSAM.RRDS` | ESDSRRDS |  |  |  |

## 7. End-to-End Data Lineage Flows

### 7.1 Initial load / refresh of masters

```mermaid
flowchart LR
  A[/ACCTDATA.PS/] -->|ACCTFILE REPRO| AV[(ACCTDATA KSDS)]
  C[/CARDDATA.PS/] -->|CARDFILE REPRO + BLDINDEX| CV[(CARDDATA KSDS + AIX)]
  U[/CUSTDATA.PS/] -->|CUSTFILE REPRO| UV[(CUSTDATA KSDS)]
  X[/CARDXREF.PS/] -->|XREFFILE REPRO + BLDINDEX| XV[(CARDXREF KSDS + AIX)]
  T0[/DALYTRAN.PS.INIT/] -->|TRANFILE REPRO + BLDINDEX| TV[(TRANSACT KSDS + AIX)]
  D[/DISCGRP.PS/] -->|DISCGRP| DV[(DISCGRP KSDS)]
  B[/TCATBALF.PS/] -->|TCATBALF| BV[(TCATBALF KSDS)]
  S[/in-stream users/] -->|DUSRSECJ| SV[(USRSEC KSDS)]
  DB2T[(DB2 TRANSACTION_TYPE)] -->|TRANEXTR DSNTIAUL| TT[/TRANTYPE.PS/] -->|TRANTYPE| TTV[(TRANTYPE KSDS)]
  DB2C[(DB2 TRANSACTION_TYPE_CATEGORY)] -->|TRANEXTR DSNTIAUL| TC[/TRANCATG.PS/] -->|TRANCATG| TCV[(TRANCATG KSDS)]
  MNT[/INPFILE maintenance records/] -->|MNTTRDB2 COBTUPDT| DB2T
  ONL[COTRTLIC / COTRTUPC online] --> DB2T
```

### 7.2 Daily transaction posting (POSTTRAN / CBTRN02C)

```mermaid
flowchart LR
  DT[/DALYTRAN.PS<br/>daily card transactions/] --> P[CBTRN02C]
  XR[(CARDXREF KSDS)] -->|lookup card -> account| P
  P <-->|read + rewrite balance, cycle credit/debit| AC[(ACCTDATA KSDS)]
  P <-->|create or add to category balance| TB[(TCATBALF KSDS)]
  P -->|valid: write posted txn| TR[(TRANSACT KSDS)]
  P -->|invalid: record + reason 100-109| RJ[/DALYREJS GDG +1/]
```

### 7.3 Monthly interest cycle (INTCALC -> COMBTRAN)

```mermaid
flowchart LR
  TB[(TCATBALF KSDS)] --> I[CBACT04C<br/>PARM run date]
  XR[(CARDXREF KSDS + AIX)] --> I
  DG[(DISCGRP KSDS<br/>rate by group/type/cat)] --> I
  I <-->|+ interest, reset cycle totals| AC[(ACCTDATA KSDS)]
  I -->|interest transactions| ST[/SYSTRAN GDG +1/]
  TR[(TRANSACT KSDS)] -->|TRANBKP REPROC| BK[/TRANSACT.BKUP GDG/]
  BK --> CM[COMBTRAN SORT by TRAN-ID]
  ST --> CM
  CM --> CO[/TRANSACT.COMBINED GDG/] -->|IDCAMS REPRO| TR
```

### 7.4 Reporting and statements

```mermaid
flowchart LR
  UI[CORPT00C screen CR00] -->|JCL via TDQ JOBS / internal reader| TJ[TRANREPT job]
  TR[(TRANSACT KSDS)] -->|REPROC| BK[/TRANSACT.BKUP GDG/] -->|SORT date filter| DY[/TRANSACT.DALY GDG/]
  DY --> R3[CBTRN03C]
  XR[(CARDXREF)] --> R3
  TT[(TRANTYPE KSDS)] --> R3
  TC[(TRANCATG KSDS)] --> R3
  DP[/DATEPARM/] --> R3
  R3 --> RP[/TRANREPT GDG report/]
  TR -->|CREASTMT SORT by card| TX[(TRXFL KSDS)] --> S3[CBSTM03A + CBSTM03B]
  XR --> S3
  AC[(ACCTDATA)] --> S3
  CU[(CUSTDATA)] --> S3
  S3 --> SP[/STATEMNT.PS text/] -->|TXT2PDF1| PDF[/STATEMNT.PS.PDF/]
  S3 --> SH[/STATEMNT.HTML/]
  TB[(TCATBALF)] -->|PRTCATBL REPROC + SORT| CB[/TCATBALF.REPT/]
```

### 7.5 Card authorization (optional IMS/DB2/MQ module)

```mermaid
flowchart LR
  RQ[/MQ request queue/] -->|trigger CP00| A[COPAUA0C]
  XR[(CCXREF)] --> A
  AC[(ACCTDAT)] --> A
  CU[(CUSTDAT)] --> A
  A <-->|GU/REPL summary, ISRT detail| IMS[(IMS DBPAUTP0<br/>PAUTSUM0 / PAUTDTL1)]
  A -->|MQPUT1 decision| RP[/MQ reply queue/]
  A -->|errors| CSSL[/TDQ CSSL/]
  IMS --> S0[COPAUS0C summary CPVS]
  IMS <--> S1[COPAUS1C detail CPVD]
  S1 -->|LINK| S2[COPAUS2C] -->|INSERT/UPDATE| FR[(DB2 AUTHFRDS)]
  IMS <-->|expire + delete| PG[CBPAUP0C via CBPAUP0J]
  IMS -->|PAUDBUNL / DBUNLDGS| UF[/PAUTDB root + child files/] -->|PAUDBLOD| IMS
```

### 7.6 Branch migration (export / import)

```mermaid
flowchart LR
  CU[(CUSTDATA)] --> E[CBEXPORT]
  AC[(ACCTDATA)] --> E
  XR[(CARDXREF)] --> E
  TR[(TRANSACT)] --> E
  CA[(CARDDATA)] --> E
  E --> X[(EXPORT.DATA KSDS<br/>multi-record C/A/X/T/D)] --> I[CBIMPORT]
  I --> CI[/CUSTDATA.IMPORT/]
  I --> AI[/ACCTDATA.IMPORT/]
  I --> XI[/CARDXREF.IMPORT/]
  I --> TI[/TRANSACT.IMPORT/]
  I --> ER[/IMPORT.ERRORS/]
```

Note: CBIMPORT opens a CARDOUT file but CBIMPORT.jcl has no CARDOUT DD - card records cannot be written as provided.

## 8. Job Scheduling Dependencies

### 8.1 Control-M (`app/scheduler/CardDemo.controlm`)

- **Daily (Control-M "TransactionBackup")**: CLOSEFIL -> TRANBKP -> WAITSTEP -> OPENFIL
- **Weekly (Control-M "TransactionTypesDBRefresh")**: MNTTRDB2 -> TRANEXTR
- **Weekly (Control-M "DisclosureGroupsRefresh")**: CLOSEFIL -> DISCGRP -> WAITSTEP -> OPENFIL
- **Monthly (Control-M "InterestCalculation")**: CLOSEFIL -> INTCALC -> COMBTRAN -> WAITSTEP -> OPENFIL

### 8.2 CA-7 trigger chains (`app/scheduler/CardDemo.ca7`)

- CLOSEFIL -> CBPAUP0J -> POSTTRAN -> WAITSTEP -> OPENFIL
- TRANTYPE / TRANCATG / TCATBALF reference reloads triggered after CLOSEFIL variants (CLOSEFIL1 / CLOSEFIL2), each followed by WAITSTEP -> OPENFIL
- CLOSEFIL -> READACCT -> READCARD -> READCUST -> READXREF -> WAITSTEP -> OPENFIL
- CREASTMT -> TXT2PDF1 -> WAITSTEP -> OPENFIL
- CLOSEFIL -> PRTCATBL -> WAITSTEP -> OPENFIL

### 8.3 README full-batch order

CLOSEFIL -> ACCTFILE -> CARDFILE -> XREFFILE -> CUSTFILE -> TRANBKP -> (TRANEXTR) -> TRANCATG -> TRANTYPE -> DISCGRP -> TCATBALF -> DUSRSECJ -> **POSTTRAN** -> **INTCALC** -> TRANBKP -> **COMBTRAN** -> **CREASTMT** -> TRANIDX -> OPENFIL (also scripted in `scripts/run_full_batch.sh`).

```mermaid
flowchart LR
  CF[CLOSEFIL] --> POST[POSTTRAN] --> INT[INTCALC] --> BKP[TRANBKP] --> CMB[COMBTRAN] --> STM[CREASTMT] --> IDX[TRANIDX] --> OF[OPENFIL]
  STM --> PDF[TXT2PDF1]
```

**Online/batch coupling**: batch jobs that update ACCTDATA, CARDDATA, CUSTDATA, CARDXREF and TRANSACT require CICS files to be closed first (CLOSEFIL / OPENFIL via SDSF `CEMT SET FIL ... CLO/OPE`). There is no record-level sharing (RLSACCESS(NO) in the CSD), so batch windows mean online downtime.

## 9. External Interfaces

| Interface | Direction | Mechanism | Producer -> Consumer |
|---|---|---|---|
| Daily transaction feed | Inbound | Sequential file DALYTRAN.PS | Card network extract -> POSTTRAN |
| Card authorization | Inbound / outbound | IBM MQ request/reply (trigger) | Network/POS -> COPAUA0C -> reply queue |
| Account / date inquiry | Inbound / outbound | IBM MQ request/reply | MQ client -> COACCT01 / CODATE01 |
| Online report submission | Internal | CICS TD queue JOBS -> JES internal reader | CORPT00C -> TRANREPT job |
| Error log | Internal | CICS TD queue CSSL | COPAUA0C -> operations |
| Statements | Outbound | Text, HTML and PDF datasets | CREASTMT/TXT2PDF1 -> print/e-delivery |
| Branch migration | Outbound / inbound | Multi-record export file | CBEXPORT -> CBIMPORT |
| FTP | Outbound | z/OS FTP client | FTPJCL -> remote host |
