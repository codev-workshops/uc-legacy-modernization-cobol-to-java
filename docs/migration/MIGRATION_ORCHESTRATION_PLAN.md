# Migration Orchestration Plan — CardDemo COBOL-to-Java

Orchestration runbook for Lead Devin executing the full CardDemo migration using the Lead/Junior Devin model.

Cross-reference these sibling docs for task details:

| Doc | Purpose |
|-----|---------|
| `APPLICATION_INVENTORY.md` | Full program/copybook/JCL catalog |
| `DATA_DICTIONARY.md` | Field-level COBOL PIC → Java type mappings |
| `DEPENDENCY_MAP.md` | Call graphs, dataset lineage, batch pipeline |
| `HOTSPOT_REPORT.md` | Complexity ranking, wave priorities, entity mapping |
| `AGENTS.md` | Per-task specs, global constraints, acceptance criteria |

---

## 1. Roles

### Lead Devin (Orchestrator)

- Owns the entire migration end-to-end
- Creates batch feature branches from `future_java_migration`
- Reads `AGENTS.md` for per-task specs and dispatches Junior Devins
- Cross-references the four sibling docs: `APPLICATION_INVENTORY.md`, `DATA_DICTIONARY.md`, `DEPENDENCY_MAP.md`, `HOTSPOT_REPORT.md`
- Monitors Junior Devin sessions for blockers
- Steps in to fix problems or provide guidance when Junior Devins are stuck
- Merges Junior work into the batch branch, resolves conflicts
- Runs test-harness validation (`cd test-harness && mvn test`) before opening PR
- Opens one PR per batch to `future_java_migration`
- Ensures no merge conflicts with `future_java_migration` before and during merge
- After each batch PR merges, rebases the next batch branch onto latest `future_java_migration`

### Junior Devin (Executor)

- Receives a scoped task from Lead Devin (one or more COBOL programs to migrate)
- Reads the specified COBOL source files in `app/cbl/` and copybooks in `app/cpy/`
- Writes Java classes under `src/main/java/com/carddemo/` with sub-packages: `entity`, `batch`, `service`, `util`, `dto`, `web`
- Writes unit tests under `src/test/java/com/carddemo/`
- Commits to a Junior-specific branch (e.g., `batch1/junior-a/entities`)
- Reports completion or blockers to Lead Devin

---

## 2. Branching Strategy

- Each batch gets a feature branch: `batch-N/migration` (e.g., `batch-1/foundation`)
- Each Junior Devin works on a sub-branch: `batch-N/junior-X/task-name`
- Lead Devin merges Junior sub-branches into `batch-N/migration`
- Lead Devin opens PR from `batch-N/migration` → `future_java_migration`
- After PR merge, Lead rebases next batch branch onto updated `future_java_migration`
- No batch starts until the previous batch's PR is merged (hard gate)

---

## 3. Conflict Prevention Rules

- **Batch 1 is a hard gate** — all entity POJOs, DateValidationUtil, and LookupCodes must merge before any Wave 1 task starts. This ensures shared classes are stable.
- Within each batch, Junior Devins write to **non-overlapping Java packages**. Lead assigns packages upfront:
  - `com.carddemo.entity` — only Batch 1 Junior A
  - `com.carddemo.util` — only Batch 1 Junior B and C
  - `com.carddemo.batch` — Batch 2 and 3 Juniors (each writes different service classes)
  - `com.carddemo.web` — Batch 4-6 Juniors
- If a Junior needs to modify a shared file (e.g., add a method to an existing entity), they must coordinate through Lead Devin — Lead merges that change first, then other Juniors rebase.
- Before opening a PR, Lead Devin runs `git fetch origin future_java_migration && git rebase origin/future_java_migration` on the batch branch and resolves any conflicts.
- Lead Devin must run `mvn compile` on the merged batch branch to ensure no compilation errors from conflicting imports or signatures.

---

## 4. Batch Definitions

### Batch 1 — Foundation (PR #1 → `future_java_migration`)

**Purpose**: Create all shared Java classes that every subsequent batch depends on.

| Junior | Task | Inputs (COBOL) | Outputs (Java) | Parallel? |
|--------|------|----------------|----------------|-----------|
| Junior A | Convert 15 copybook record layouts to Java entity POJOs/records | `app/cpy/CVACT01Y.cpy`, `CVACT02Y.cpy`, `CVACT03Y.cpy`, `CVCUS01Y.cpy`, `CVTRA05Y.cpy`, `CVTRA06Y.cpy`, `CVTRA01Y.cpy`, `CVTRA02Y.cpy`, `CVTRA03Y.cpy`, `CVTRA04Y.cpy`, `CVTRA07Y.cpy`, `CVEXPORT.cpy`, `COCOM01Y.cpy`, `CSUSR01Y.cpy`, `CUSTREC.cpy` | `com.carddemo.entity.*` — AccountRecord, CardRecord, CardXrefRecord, CustomerRecord, TransactionRecord, DailyTransactionRecord, TranCatBalRecord, DiscGroupRecord, TranTypeRecord, TranCatRecord, ReportLayoutRecord, ExportRecord, CardDemoCommarea, SecUserData, CustomerRecordFD | Yes |
| Junior B | Create date validation utility | `app/cbl/CSUTLDTC.cbl`, `app/cpy/CSUTLDPY.cpy`, `app/cpy/CSUTLDWY.cpy`, `app/cpy/CSDAT01Y.cpy` | `com.carddemo.util.DateValidationUtil` | Yes |
| Junior C | Convert lookup codes | `app/cpy/CSLKPCDY.cpy` (1318 lines of 88-level values) | `com.carddemo.util.LookupCodes` | Yes |

**Lead actions:**
1. Create `batch-1/foundation` branch from `future_java_migration`
2. Spawn all 3 Juniors in parallel (no inter-dependencies)
3. Merge all 3 Junior branches into `batch-1/foundation`
4. Run `mvn compile` to verify all entities compile
5. Open PR to `future_java_migration`
6. **GATE: Do not start Batch 2 until this PR merges**

---

### Batch 2 — Batch Core (PR #2 → `future_java_migration`)

**Purpose**: Migrate the 4 highest-value batch programs (transaction processing pipeline).

| Junior | Task | COBOL Program | Outputs (Java) | Parallel? |
|--------|------|---------------|----------------|-----------|
| Junior A | Transaction posting | `CBTRN02C.cbl` (731 LOC, 6 files, 6 copybooks) | `com.carddemo.batch.TransactionPostingService` | Yes |
| Junior B | Transaction validation | `CBTRN01C.cbl` (6 files, 5 copybooks) | `com.carddemo.batch.TransactionValidationService` | Yes |
| Junior C | Transaction report | `CBTRN03C.cbl` (6 files, 5 copybooks) | `com.carddemo.batch.TransactionReportService` | Yes |
| Junior D | Interest calculation | `CBACT04C.cbl` (5 files, 5 copybooks) | `com.carddemo.batch.InterestCalculationService` | Yes |

**Lead actions:**
1. Rebase `batch-2/batch-core` onto merged `future_java_migration` (which now has Batch 1 entities)
2. Spawn all 4 Juniors in parallel — they write to different service classes in the same package but different files
3. **Watch out for Junior D (CBACT04C)**: LINKAGE SECTION with external date param, COMPUTE-based financial math — verify BigDecimal usage with HALF_UP rounding
4. **Watch out for Junior A (CBTRN02C)**: Most complex batch program — nested IF for validation, 6-file I/O coordination
5. After merge, run `cd test-harness && mvn test` — the test harness has specific CBTRN02C and CBACT04C validators
6. Open PR to `future_java_migration`
7. **GATE: Do not start Batch 3 until this PR merges**

---

### Batch 3 — Batch Supporting (PR #3 → `future_java_migration`)

**Purpose**: Migrate remaining batch programs (statement generation, account read, export/import).

| Junior | Task | COBOL Program(s) | Outputs (Java) | Parallel? |
|--------|------|-------------------|----------------|-----------|
| Junior A | Statement generation | `CBSTM03A.CBL` + `CBSTM03B.CBL` | `com.carddemo.batch.StatementGenerationService` | Yes |
| Junior B | Account read/export | `CBACT01C.cbl` | `com.carddemo.batch.AccountReadService` | Yes |
| Junior C | Export + Import pipeline | `CBEXPORT.cbl` + `CBIMPORT.cbl` | `com.carddemo.batch.ExportService`, `com.carddemo.batch.ImportService` | Yes |

**Lead actions:**
1. **CRITICAL: Junior A (CBSTM03A) will likely need Lead intervention** — CBSTM03A uses ALTER/GO TO which is the hardest control flow pattern to convert. Lead should proactively provide guidance: "Do NOT replicate ALTER semantics. Refactor to a state enum with switch/case or if/else." Also has 2D array handling and PSA addressing.
2. Junior B: Watch for COMP-3 variables and CALL to assembler program COBDATFT — replace with `java.time.format.DateTimeFormatter`
3. Junior C: Watch for REDEFINES in CVEXPORT.cpy — use sealed interface with subclasses per record type
4. After merge, run test-harness CBACT01C validation
5. Open PR to `future_java_migration`
6. **GATE: Do not start Batch 4 until this PR merges**

---

### Batch 4 — CICS Navigation + Read-Only Screens (PR #4 → `future_java_migration`)

**Purpose**: Migrate the application entry point and read-only CICS screens to Spring Boot.

| Junior | Task | COBOL Program(s) | Outputs (Java) | Parallel? |
|--------|------|-------------------|----------------|-----------|
| Junior A | Sign-on + Menus | `COSGN00C.cbl`, `COMEN01C.cbl`, `COADM01C.cbl` | `com.carddemo.web.SignOnController`, `com.carddemo.web.MainMenuController`, `com.carddemo.web.AdminMenuController` | Yes (after Spring Boot scaffold) |
| Junior B | Read-only screens | `COACTVWC.cbl`, `COCRDSLC.cbl`, `COTRN00C.cbl`, `COTRN01C.cbl` | `com.carddemo.web.AccountViewController`, `com.carddemo.web.CardDetailController`, `com.carddemo.web.TransactionListController`, `com.carddemo.web.TransactionDetailController` | Yes (after Spring Boot scaffold) |

**Lead actions:**
1. **Lead must create the Spring Boot scaffold first** before spawning Juniors: pom.xml with Spring Boot + Spring Web + Spring Data dependencies, application.yml, base configuration classes, security config for session/auth.
2. Map COMMAREA (`COCOM01Y.cpy` → `CardDemoCommarea.java` from Batch 1) to session state or request-scoped DTO
3. Map BMS maps to REST API DTOs
4. Map XCTL to controller redirect / dispatch
5. Junior B depends on Junior A's navigation controllers being in place for XCTL-back-to-menu flows — Lead should merge Junior A first, then start Junior B, OR have Junior B stub the menu return endpoints
6. Open PR to `future_java_migration`

---

### Batch 5 — CICS User Management (PR #5 → `future_java_migration`)

**Purpose**: Migrate user CRUD screens.

| Junior | Task | COBOL Program(s) | Outputs (Java) | Parallel? |
|--------|------|-------------------|----------------|-----------|
| Junior A | User list + add + update + delete | `COUSR00C.cbl`, `COUSR01C.cbl`, `COUSR02C.cbl`, `COUSR03C.cbl` | `com.carddemo.web.UserListController`, `com.carddemo.web.UserAddController`, `com.carddemo.web.UserUpdateController`, `com.carddemo.web.UserDeleteController` | Single Junior (tightly coupled) |

**Lead actions:**
1. These 4 programs share USRSEC VSAM access and are tightly coupled — one Junior is better than splitting
2. Ensure password hashing (bcrypt) for `SEC-USR-PWD` field — never store cleartext
3. Ensure PII masking for `CUST-SSN`, `CARD-NUM`, `CARD-CVV-CD`
4. Open PR to `future_java_migration`

---

### Batch 6 — CICS Full CRUD (PR #6 → `future_java_migration`)

**Purpose**: Migrate the most complex CICS programs — deep validation, state machines, BMS interactions.

| Junior | Task | COBOL Program(s) | Outputs (Java) | Parallel? |
|--------|------|-------------------|----------------|-----------|
| Junior A | Card list + update | `COCRDLIC.cbl` (1,459 LOC), `COCRDUPC.cbl` (1,560 LOC) | `com.carddemo.web.CardListController`, `com.carddemo.web.CardUpdateController` | Yes |
| Junior B | Transaction add + Bill payment + Reports | `COTRN02C.cbl` (783 LOC), `COBIL00C.cbl`, `CORPT00C.cbl` | `com.carddemo.web.TransactionAddController`, `com.carddemo.web.BillPaymentController`, `com.carddemo.web.ReportController` | Yes |
| **Lead** | Account update (most complex program) | `COACTUPC.cbl` (4,236 LOC, 16 copybooks, nesting depth 5+) | `com.carddemo.web.AccountUpdateController` | After Junior A + B |

**Lead actions:**
1. **Lead takes COACTUPC directly** — it is the single most complex program in the entire application (4,236 lines, 16 copybooks, multi-level EVALUATE TRUE state machine). This should NOT be delegated to a Junior.
2. COACTUPC has states: ACUP-DETAILS-NOT-FETCHED, SHOW-DETAILS, CHANGES-MADE, CHANGES-OKAYED — map to a Java enum with switch
3. Junior A: COCRDLIC has complex page-up/down browse logic — map to paginated repository queries
4. Junior B: CORPT00C submits batch JCL via TDQ — map to Spring Batch `JobLauncher` async invocation. COTRN02C CALLs CSUTLDTC — use the DateValidationUtil from Batch 1.
5. After all work is merged, run full test suite
6. Open final PR to `future_java_migration`

---

## 5. Lead Devin Checklist Per Batch

Exact sequence Lead Devin follows for every batch:

```
1. git checkout future_java_migration && git pull
2. git checkout -b batch-N/<name>
3. Review AGENTS.md tasks for this batch
4. Spawn Junior Devins with task specs (provide: COBOL source paths, copybook paths, output Java class names, constraints, acceptance criteria)
5. Monitor Junior sessions — check for:
   - Compilation errors (wrong imports, missing entity classes)
   - Wrong type mappings (float/double instead of BigDecimal for financial fields)
   - Control flow issues (trying to replicate ALTER/GO TO literally)
   - Missing edge cases (FILE STATUS error handling, leap year dates)
6. When a Junior is stuck:
   - If it's a type mapping issue → point them to DATA_DICTIONARY.md PIC-to-Java table
   - If it's a dependency issue → point them to DEPENDENCY_MAP.md call graph
   - If it's a structural issue (ALTER/GO TO, REDEFINES) → take over that portion
7. As Juniors complete, merge their branches:
   git merge --no-ff batch-N/junior-X/task-name
8. After all Juniors merged:
   mvn compile  (must pass — no compilation errors)
   mvn test     (all unit tests pass)
   cd test-harness && mvn test  (for batch program tasks)
9. Rebase onto latest future_java_migration:
   git fetch origin future_java_migration
   git rebase origin/future_java_migration
   (resolve any conflicts)
10. Open PR: batch-N/<name> → future_java_migration
    PR title: "Batch N: <description>"
    PR body: list all migrated programs, test results, any known issues
11. After PR merges, start next batch from step 1
```

---

## 6. Escalation Rules for Lead Devin

When to step in vs. advise:

| Situation | Action |
|-----------|--------|
| Junior uses `double`/`float` for financial math | **Advise**: Point to DATA_DICTIONARY.md, instruct to use `BigDecimal` |
| Junior can't handle ALTER/GO TO (CBSTM03A) | **Take over**: Refactor control flow manually |
| Junior's test-harness validation fails with field mismatches | **Advise**: Point to specific field in DATA_DICTIONARY.md, check COMP-3 decoding |
| Junior creates merge conflict with another Junior | **Step in**: Merge the first Junior's branch, rebase the second |
| Junior is stuck on REDEFINES mapping | **Advise**: Use sealed interface pattern, provide code skeleton |
| Junior's compilation fails due to missing entity class | **Step in**: Check if Batch 1 entities are on the branch, rebase if needed |
| COACTUPC migration (Batch 6) | **Lead does this directly** — do not delegate |

---

## 7. Success Criteria Per Batch PR

Each PR must satisfy before merge:

- [ ] `mvn compile` passes with zero errors
- [ ] All new unit tests pass (`mvn test`)
- [ ] For batch tasks: `cd test-harness && mvn test` passes (64+ tests)
- [ ] No `double`/`float` used for financial fields (grep check)
- [ ] No cleartext passwords stored (grep for `SEC-USR-PWD` handling)
- [ ] Commit messages follow format: `migrate(<task-id>): <short description>`
- [ ] No merge conflicts with `future_java_migration` HEAD
- [ ] PR description lists all migrated COBOL programs and their Java counterparts
