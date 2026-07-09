## Decisions

### D1. Single-repo scope. Phase-1 concerns are follow-ups.

The internal audit `dataid-template-runtime.pdf` lists five discrepancies. Four are actionable in this repo (F1–F4 in `proposal.md`). The fifth (F5) — the payload-whitelist trim in `judo-ui-typescript-rest-template/.../common/utils.ts.hbs:5–27` and the latent `__version` typeof bug at line 15 — is physically located in the sister repo and touches zero source lines here.

Rejected: a combined two-repo openspec change with a hard cross-repo dependency. Reason: the stated goal of the audit is *Playwright-test parity*. Playwright never inspects POST bodies (F2.2 in the PDF) and this template has no reference to `__version` (F2.4). Neither Phase-1 concern blocks test parity today. Deferring them keeps this change reviewable in one commit series and avoids a coordinated PR chain.

Kept as a compat rung inside `isNewRow` and `resolveTransferId`: Phase 1's serializer at `rest/serializer.ts.hbs:92–93` still auto-seeds `__identifier = "draft:<uuid>"` on server responses that arrive without an identifier (rare, mostly embedded transients). Those rows would otherwise fall through the `__isNew` check silently. The compat rung is one line — `row.__identifier?.startsWith(DRAFT_PREFIX)` — and is annotated `// TODO(JNG-XXXX): remove after Phase-1 draft-seed retired`. Two follow-up JIRA tickets should be filed: one to trim `applyStoredMembers`, one to migrate the deserializer's auto-seed to `__tempId`/`__isNew`.

### D2. New `transfer-identity` capability, not folded into `data-tables`.

The identity contract is genuinely cross-cutting: it defines the row key (`data-tables`, `relation-management`), the new-row semantics (`data-tables`, `input-widgets` via inline-create), and the `data-testid` hierarchy (`data-tables`, `input-widgets`, `relation-management`, plus every layout template that carries a test id). Folding these requirements into any one existing capability would create sibling-refers-to-sibling coupling in the specs.

Rejected: extending `data-tables` to own testid rules. Reason: `data-tables` is scoped to MUI DataGrid generation; the testid vocabulary applies uniformly to every emission site, most of which are outside tables (drawer nav, breadcrumbs, dialog buttons, widget outer wrappers).

Rejected: two separate new capabilities (`row-identity` + `test-data-ids`). Reason: they share the same primitive (`resolveTransferId`). Two capabilities would duplicate the fallback-chain requirement in prose, or one would `SEE ALSO` the other — both worse than a single capability owning both.

### D3. Helpers live in a per-actor `.ts.hbs`, not in a Java helper class.

The four `build*TestId` helpers are pure string functions with no dependency on the EMF model at code-generation time (the XMI id is known at render time, but the *format* is trivial). Placing them in TypeScript keeps the format visible to reviewers, testable with vitest, and portable to the runtime (which will use identical prose to align).

Rejected: emit each testid as a literal string from a Java helper (`UiTestIdHelper.fieldTestId(widget)`). Reason: every consumer would need to plumb the widget model through the template context, and format drift between Java and TypeScript would be invisible. The one-source-of-truth is the runtime's TS implementation; keeping this template's implementation also in TS aligns them.

### D4. Row testids embed `resolveTransferId(row)`, not `row.__identifier`.

The audit's flagship requirement is that a single Playwright selector `getByTestId('table::<id>::row::<key>')` targets the same row across both engines. The runtime's row-key resolver returns `__signedIdentifier ?? __identifier ?? __tempId ?? id`; for a saved row it returns `__signedIdentifier`, for a client-created row it returns `__tempId`. Any template site that bakes `row.__identifier` into a testid loses parity as soon as `__identifier` is absent (all client-created rows) or renamed (all Phase-1-deserializer-seeded transients).

Rejected: use `__signedIdentifier` alone. Reason: not present on client-created rows before save. Playwright cannot target such rows for interaction.

### D5. Non-null-assertion cleanup is mandatory, not incidental.

The ~20 `row.__identifier!` sites are latent bugs (`TypeError: Cannot read properties of undefined`). The template ships them because the current draft-seeding scheme *guarantees* the field is set — but the guarantee is not visible in the code. Under the new scheme `__identifier` is intentionally absent on client-created rows, and the guarantee is gone. Every non-null-asserted site becomes a real runtime error surface. Routing through `resolveTransferId` — which never returns `undefined` — resolves the class of bugs, not just the trigger.

Rejected: fix only the sites that would break under the new seed scheme. Reason: partial cleanup leaves the fragile pattern in the codebase, waiting for the next contract change to re-introduce the bug. The audit trail is cleaner if the assertion is retired everywhere at once.

### D6. Rebuild coverage on a fresh branch, do not inherit `feature/JNG-6391_Test_Data-TestId`.

The existing branch is 13 commits ahead of develop and contains reverts, "sync openspec archive" bookkeeping, and F1/F3/F5/F6/F9/F10/F11/F15 patches in the *old* testid format. The format sweep will re-touch every emission site regardless. Preserving those commits gives reviewers a diff-of-a-diff (old format → new format) rather than a diff-of-source (baseline → new format). The old branch stays untouched as a reference for the coverage baseline (`tasks.md` §0.1–0.2).

Rejected: rebase and squash the old branch, then land the format sweep on top. Reason: the coverage would be double-counted in the commit history — every line the format sweep touches was already touched by an F-commit two commits ago. Reviewers gain nothing.

### D7. Snapshot churn is unavoidable and mechanical.

Every `.tsx.snapshot` that renders a re-formatted `data-testid` or a re-shaped new-row seed will drift. `judo-diff-checker-maven-plugin:checkDiffs` will enumerate the drifts on first CI run; the refresh procedure per `AGENTS.md` §5 is a byte-for-byte copy from `target/frontend-react/**` back to `src/test/resources/snapshots/frontend-react/**`. One snapshot-refresh commit per itest is recommended (six commits) to keep the review diff navigable.

Not a decision, but worth stating: no snapshot content changes because the change alters DOM output; only because the DOM output changed *intentionally* per this proposal.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| The compat rung (`isNewRow` reading `draft:` prefix) hides progress on the runtime unification if Phase 1 never gets updated. | Two follow-up JIRA tickets filed in the same PR body. The rung is annotated with a TODO referencing them and is trivial to delete (one line). |
| The `resolveTransferId` fallback chain used by tests differs from the runtime's chain by *ordering*. | The chain is copied verbatim from `dataid-template-runtime.pdf` §3. If the runtime's actual chain differs, this template is authoritatively wrong and this change would need a follow-up. Task 8.1 spot-checks this against a live runtime build. |
| Rewriting `identifierAttribute={'__identifier'}` to `getRowId={resolveTransferId}` on the relation-column DataGrid changes an MUI-typed prop from `string` to `(r) => string`. | MUI DataGrid accepts either the `getRowId` callback (canonical) or the `getRowId` returning `unknown` at the type level (`GridRowId`). No runtime shape change. Verified against `@mui/x-data-grid` 8.x types in itest node_modules. |
| The role suffix vocabulary (`::input`, `::button::set`, `::dropdown`, etc.) drifts from the runtime's over time. | The suffix vocabulary is captured in `specs/transfer-identity/spec.md` as a normative table. Any change to the vocabulary is a spec change; drift becomes a review-time signal. |
| Snapshot refresh masks a real regression (e.g. a template inadvertently drops a `data-testid` during the sweep). | The audit gate `tasks.md` §5.6 diffs the emission line counts before and after; ±5 tolerance for helper-collapse (e.g. one helper call replacing two adjacent literals). Line-count parity outside that band blocks the commit. |
| Two testids collide because the underlying XMI id is duplicated across model elements. | The role-suffixed hierarchy makes collisions structurally impossible for a *single* element (three suffixes = three testids). Genuine model-level duplicates (two widgets with the same XMI id) remain a distinct diagnostic; the change surfaces them earlier because the same-testid failure now fires on outer wrapper *and* input *and* autocomplete simultaneously. |

## Non-goals

1. No modification to `judo-ui-typescript-rest-template`. The payload whitelist trim and the `__version` typeof bug are separately-raised JIRA tickets. (D1)
2. No new `data-testid` emissions on elements that do not already carry one on `feature/JNG-6391_Test_Data-TestId`. Extending coverage is a separate ticket. (Proposal §What this change does NOT do)
3. No change to the runtime. This template conforms to the runtime; the runtime is the authoritative source. (D3, D4)
4. No new Java helper class. All identity/testid formatting is TypeScript. (D3)
5. No change to snapshot-checking semantics or the `judo-diff-checker-maven-plugin` plugin itself. Only content churn. (D7)
6. No change to model, catalogue, or template-parameter (`muiLicensePlan`, `tablePageLimit`, etc.) surface.
7. No refactor of the `feature/JNG-6391_Test_Data-TestId` branch itself. It stays as a reference. (D6)
