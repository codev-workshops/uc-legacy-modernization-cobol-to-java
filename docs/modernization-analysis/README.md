# CardDemo Modernization Analysis

Static analysis of the COBOL/CICS/JCL estate in this repository, produced as input for a COBOL-to-Java modernization.

| Document | Contents |
|---|---|
| [APPLICATION_INVENTORY.md](APPLICATION_INVENTORY.md) | Every program, copybook, BMS map, JCL job, PROC, CSD resource and supporting artifact, with kind / business-domain / size classification |
| [DATA_DICTIONARY.md](DATA_DICTIONARY.md) | Business entities with field-level layouts from copybook PIC clauses (offsets, lengths, business names, Java types, valid values), DB2/IMS structures, reference data and data-quality notes |
| [DEPENDENCY_MAP.md](DEPENDENCY_MAP.md) | Online navigation and CALL graph, program-to-copybook and program-to-data CRUD matrices, job-to-dataset I/O, end-to-end data lineage and scheduling dependencies |
| [HOTSPOT_REPORT.md](HOTSPOT_REPORT.md) | Top 10 modules ranked by complexity, risk and business impact, full ranking, cross-cutting risks and suggested modernization waves |

Scope: `app/` (core + the three optional modules) and `samples/`. Metrics count non-blank, non-comment COBOL lines (columns 8-72).
