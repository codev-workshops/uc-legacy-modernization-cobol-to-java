# CardDemo reverse-engineering artifacts

These seven Markdown artifacts describe the current COBOL system for agents and engineers preparing a Spring Boot replacement.

**Analyzed source:** [`fbcb944af892dcb8c94c0549f2ee248945010ff8`](https://github.com/codev-workshops/uc-legacy-modernization-cobol-to-java/tree/fbcb944af892dcb8c94c0549f2ee248945010ff8). Source citations use this immutable revision and physical file line numbers. The analysis covers the core application and all three optional module families, with their deployment status left unknown.

**Evidence boundary:** static source analysis. No legacy workload was executed and no runtime parity result or golden output was captured. The inventory covers all 44 tracked COBOL files; that does not imply exhaustive branch or scenario coverage. Each artifact states its own limits.

## Reading order and ownership

| Artifact | What it contains and how it helps | Owns |
| --- | --- | --- |
| [01 — System overview and component inventory](01_SYSTEM_OVERVIEW_AND_INVENTORY.md) | Business boundaries, actors, components, entry points, dependencies and core/optional scope. Start here to locate a capability. | Component inventory and names |
| [02 — End-to-end execution flows](02_END_TO_END_EXECUTION_FLOWS.md) | Online, batch and message sequences from trigger through data changes, outputs and alternate paths. Connects individual programs into operations. | `FLOW-*` IDs and execution order |
| [03 — Business rules catalog](03_BUSINESS_RULES_CATALOG.md) | Validations, financial formulas, precision, defaults, error precedence and incomplete rules. Establishes executable decisions without inventing policy. | `BR-*` IDs and predicates |
| [04 — Data model, record dictionary and lineage](04_DATA_MODEL_DICTIONARY_AND_LINEAGE.md) | Entities, copybook fields, calculated offsets, keys, encodings, DB2/IMS structures and readers/writers. Preserves both meaning and physical representation. | Field definitions and storage assumptions |
| [05 — Input, output and interface contracts](05_INTERFACE_CONTRACTS.md) | Screen fields/actions, files, reports, statements, COMMAREA, callable and MQ boundaries. Establishes what users and surrounding systems exchange. | Observable input/output contracts |
| [06 — Runtime, state and failure semantics](06_RUNTIME_STATE_AND_FAILURE_SEMANTICS.md) | Reentry, locks, writes, commit/rollback requests, error handling, partial state and reruns. Bounds correctness under interruption and concurrency. | State and recovery analysis |
| [07 — Behavioral examples, evidence and open questions](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md) | Synthetic scenarios, fixture limitations, contradictions, prioritized questions and a future evidence-capture procedure. Separates derived expectations from observed results. | Scenario IDs and the `Q-*` register |

For a single capability, read its flow, rules, data and interfaces together, then check its failure semantics and evidence gaps. The following routes are starting points rather than a second specification.

| Capability | Execution | Rules | Evidence/scenarios |
| --- | --- | --- | --- |
| Daily posting | [FLOW-DAILY-POSTING](02_END_TO_END_EXECUTION_FLOWS.md#flow-daily-posting) | [BR-POST-*](03_BUSINESS_RULES_CATALOG.md#2-daily-transaction-posting) | [P1–P4](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md#posting-decision-order-matters) |
| Interest and cycle processing | [FLOW-INTEREST-CYCLE](02_END_TO_END_EXECUTION_FLOWS.md#flow-interest-cycle) | [BR-INTEREST-*](03_BUSINESS_RULES_CATALOG.md#3-interest-and-cycle-processing) | [Financial examples](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md#financial-examples), I1–I3 |
| Bill payment | [FLOW-BILL-PAYMENT](02_END_TO_END_EXECUTION_FLOWS.md#flow-bill-payment) | [BR-PAY-*](03_BUSINESS_RULES_CATALOG.md#4-online-bill-payment) | [Financial examples](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md#financial-examples), B1–B3 |
| Account/customer and card maintenance | [FLOW-ACCOUNT-UPDATE](02_END_TO_END_EXECUTION_FLOWS.md#flow-account-update), [FLOW-CARD-UPDATE](02_END_TO_END_EXECUTION_FLOWS.md#flow-card-update) | [Account](03_BUSINESS_RULES_CATALOG.md#5-account-and-customer-maintenance), [card](03_BUSINESS_RULES_CATALOG.md#6-card-and-transaction-maintenance) | [Online maintenance](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md#online-maintenance-examples) |
| Reports, statements and interchange | [Batch report](02_END_TO_END_EXECUTION_FLOWS.md#flow-batch-report), [statements](02_END_TO_END_EXECUTION_FLOWS.md#flow-statements), [export/import](02_END_TO_END_EXECUTION_FLOWS.md#flow-export-import) | [Reports](03_BUSINESS_RULES_CATALOG.md#8-reports-and-statements), [interchange](03_BUSINESS_RULES_CATALOG.md#9-importexport-semantics) | [Batch and interchange](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md#batch-report-and-interchange-examples) |
| Optional DB2, IMS and MQ capabilities | [Flow map](02_END_TO_END_EXECUTION_FLOWS.md#flow-map) | [DB2](03_BUSINESS_RULES_CATALOG.md#10-optional-db2-transaction-types), [authorization](03_BUSINESS_RULES_CATALOG.md#11-optional-authorization-decisions), [purge/fraud](03_BUSINESS_RULES_CATALOG.md#12-authorization-purge-and-fraud-lifecycle), [MQ inquiries](03_BUSINESS_RULES_CATALOG.md#13-optional-vsammq-services) | [Optional scenarios](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md#optional-module-examples) |

## Shared evidence vocabulary

The long labels and short codes mean the same thing across the set.

| Code | Label and aliases | Meaning |
| --- | --- | --- |
| S | Source-confirmed; declaration/configuration evidence | The cited source contains the statement or definition. This does not prove installation or successful execution. |
| I | Inferred; derived; calculated | A consequence, synthetic result or byte calculation under stated assumptions. It has not been runtime-observed. |
| D | README/comment claim; documentation claim | Described intent or historical metadata. Check executable evidence separately. |
| R | Runtime-observed | A captured result with an identified executable baseline. **There are no R results in this set.** |
| U | Unknown; open question | Evidence is missing or a decision remains unresolved. |

Identifiers, program names and source spellings are preserved, including `EXPIRAION`. DB2 and Db2 refer to the same subsystem. “Account,” “card,” “customer” and “user” denote separate records/identities. CICS FILE names, COBOL SELECT/ASSIGN names and JCL DD/dataset names are distinct; use the dictionaries and bindings rather than matching similar names.

## Reconciled behavior that must stay consistent

These source findings are carried across the artifacts. The linked owners contain the citations and qualifications.

| Concern | Consolidated reading |
| --- | --- |
| Posting rejection and writes | [Posting flow](02_END_TO_END_EXECUTION_FLOWS.md#flow-daily-posting) validates first, then changes category balance, rewrites account, and writes the transaction. Reason `109` belongs to a late account-rewrite failure; it is not a duplicate-transaction rejection. |
| Last interest account | [BR-INTEREST-FINAL-GROUP](03_BUSINESS_RULES_CATALOG.md#3-interest-and-cycle-processing) traces the test-before loop: the apparent EOF update in its outer ELSE is not reached on the normal EOF transition. Do not assume a final-group flush. |
| Payment failure | [Payment state/failure analysis](06_RUNTIME_STATE_AND_FAILURE_SEMANTICS.md#6-bill-payment-and-transaction-addition) shows that handled transaction-write failure can still reach account rewrite. A screen send does not exit the caller. |
| Export/import representation | [Data dictionary](04_DATA_MODEL_DICTIONARY_AND_LINEAGE.md#5-export-and-import) distinguishes the calculated 500-byte envelope and sequence offset 27 from JCL key offset 28, plus FD/key-association and missing-DD questions. |
| MQ atomicity | [Optional subsystem semantics](06_RUNTIME_STATE_AND_FAILURE_SEMANTICS.md#8-optional-subsystem-errors-and-recovery) separates authorization NO-SYNCPOINT receive/reply from inquiry SYNCPOINT operations. A later CICS syncpoint does not retroactively include the former. |
| DB2 batch termination | [BR-DB2-TYPE-BATCH](03_BUSINESS_RULES_CATALOG.md#10-optional-db2-transaction-types) preserves the named abend paragraph's RC/EXIT behavior; its name does not establish an abend or rollback. |
| Deployment and scheduling | [FLOW-SCHEDULING](02_END_TO_END_EXECUTION_FLOWS.md#flow-scheduling) preserves differences among scripts, JCL, CA7 and Control-M. Installed CSD recovery settings and active schedules remain unknown. |

## Using and maintaining the set

1. Identify the capability and whether optional-module behavior is in scope for the replacement.
2. Trace requirements through `FLOW-*`, `BR-*`, physical records, interface contracts and source citations. Scenario IDs in document 07 are local; include the document link when citing them.
3. Consult the [stable question register](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md#stable-questions-register) before treating an assumption as a requirement. Its P0/P1/P2 values prioritize evidence collection, not confirmed production-incident severity. Keep specialized deployment/layout questions in their owning documents and cross-link them when adding a shared `Q-*` entry.
4. Use the [legacy evidence capture procedure](07_BEHAVIORAL_EVIDENCE_AND_OPEN_QUESTIONS.md#future-legacy-evidence-capture-procedure) to turn selected inferred scenarios into observed results when an authorized environment is available.
5. Record decisions to correct suspected legacy defects separately from the recovered behavior. A future Spring Boot design should identify which behavior it preserves and which approved decision changes it.
6. When source changes, update affected owners and references together, retain stable IDs, and record the newly analyzed revision. Do not silently retarget citations to a moving branch.
