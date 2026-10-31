# CardDemo COBOL to Java 17 Migration Blueprint

| Document | Contents |
|---|---|
| [MIGRATION_BLUEPRINT.md](MIGRATION_BLUEPRINT.md) | Target architecture, strategy (strangler / rewrite / refactor / replatform / retire) per functional area, COBOL -> Java 17 construct mapping, data migration and verification strategy |
| [DOMAIN_DECOMPOSITION.md](DOMAIN_DECOMPOSITION.md) | 12 bounded contexts with overview and per-context diagrams, context map, data-coupling analysis, 14 extraction seams with ratings and extraction order |
| [CUTOVER_PLAN.md](CUTOVER_PLAN.md) | Phases 0-7 from lowest to highest risk, with entry/exit criteria, cutover mechanism, rollback and program disposition |
| [RISK_REGISTER.md](RISK_REGISTER.md) | Top 18 risks with likelihood x impact scores, evidence, mitigations and contingencies |

Inputs: the static analysis in [`../modernization-analysis`](../modernization-analysis/README.md) (added in a separate PR).
