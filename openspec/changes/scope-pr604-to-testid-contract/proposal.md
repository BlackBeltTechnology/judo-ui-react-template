## Why

PR #604 is the generator-side half of JNG-6411. The goal is one Playwright suite (Tatami) that runs unchanged against both frontends of a model: the generated React app and `judo-frontend-runtime`. A project serves one frontend at a time, at the same `/{model}/{actor}` URL; after the migration, switching between the two is a rebuild with the other `frontendType`. For that, the generated React app must emit the same `data-testid` grammar as `@judo/test-ids`, and behave the same where a shared spec depends on behaviour. On `judo-frontend-runtime` #69 the reviewer rejected exactly this kind of scope mix (*"roll back all UI logic changes. Task was testid refactor."*). #604 carries the same mix, and it will draw the same objection:

- `19d9281b` namespaces browser storage keys, `d188686f` documents removing it, `4ebc5b35` removes it. The trio is noise (review #4, #5). The `transfer-id.test.ts` registration in `ui-react.yaml` that review #4 wanted to keep is in `0ac05f3b`, not in the trio. The namespacing also leaked into `0ac05f3b` (barrel export), `cde692e0` (`useDataStore`), `de24b430` (`RootErrorBoundary`) and `ee481bed` (`DATA_IDENTITY.md`), so those hunks go too.
- `0c23309c` (confirmation id plus a confirmation before clear and before an eager bulk delete) and the single-input date structure of `f4c258c0` change behaviour, not only ids. `861f9002` only adds filter-dialog ids and stays (review #3, #6, #7).
- 540 of 863 added `.hbs` lines carry no test-id/data-id (`git diff $(git merge-base origin/develop HEAD) HEAD -- '*.hbs'`; review #8).

#604 also has 3 conflicts with `develop` (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`), all caused by `ad0848bc` JNG-6410 (autocomplete no-results hint).

## What Changes

- Rebase #604 on `develop` and resolve the 3 widget conflicts by keeping JNG-6410's hint and adding the canonical ids.
- Drop `19d9281b`, `d188686f`, `4ebc5b35` and the namespacing hunks that leaked into later commits, `NoStorageNamespacingTest.java`, the `drop-branch-storage-namespacing` folder, **and** the `browser-state-isolation` delta of `emit-canonical-table-testids`, which would otherwise keep requiring namespacing (review #1).
- Move the behaviour hunks of `0c23309c` (confirm before clear and before an eager bulk delete) to a follow-up PR, mirroring runtime PR B. Drop the behaviour half of `f4c258c0` (`enableAccessibleFieldDOMStructure={false}`) instead of moving it: MUI X v9 removed the option, so `feature/react-followups` carries change `sectioned-date-fields` in its place. The id hunks of both commits stay in #604.
- Bring the stale changes `unify-data-id-and-testid-with-runtime` (0/41), `align-testids-with-runtime-package` (0/35) and `align-domain-testids-with-runtime` (0/35) up to date. Check off the tasks the branch already delivers, or delete the change if it was superseded by `emit-canonical-table-testids` / `align-buttons-with-runtime`.

## How the split mirrors the runtime

The runtime's #69 is split the same way (`judo-frontend-runtime` `split-pr69-by-scope`): A = ids only, B = behaviour fixes. This PR is the React counterpart of A; `feature/react-followups` is the counterpart of B. They must keep the same boundary, because a Tatami spec is accepted only when it passes on **both** engines (`judo-tatami-tests` `react-template-e2e-parity`):

| Behaviour | Runtime | React | Coupling |
|---|---|---|---|
| Sectioned date-like fields (keyboard entry, no `fill()`) | B (runtime `sectioned-date-fields`) | follow-up (`sectioned-date-fields`; `f4c258c0` behaviour half dropped) | Date specs pass on both engines only when both have landed. Merge together. |
| Confirmation policy (what asks for confirmation) | B (`230ef61` policy half) | follow-up (`0c23309c` behaviour half) | Same. |
| Table loading indicator id `table::<id>::loading` | B (prerequisite of the shared `TableHelper`) | this PR (id only) | The shared helper waits on it on both engines. |

If the reviewer rejects a behaviour on one side, it has to be rejected on the other side too, and the dependent specs are rewritten. It is never resolved with an engine conditional in a spec.

## Capabilities

These capabilities are what #604 is accountable for after the scope cleanup: test-id parity with `@judo/test-ids` that is not already covered by `emit-canonical-table-testids` (rows, cells, selection, filter panel) or `align-*` (transfer identity). The moved behaviour (confirming clear and eager bulk delete) and the sectioned date fields are specified on `feature/react-followups`, not here. Storage namespacing is dropped along with `drop-branch-storage-namespacing`; the baseline specs never had it.

### New Capabilities
None.

### Capabilities receiving new requirements (ADDED deltas; existing requirements are not changed)
- `input-widgets`: `::input` on the input that carries the value (checkbox: the inner input). The date-like structure stays as specified in "Date/Time/DateTime Input", which `sectioned-date-fields` modifies (review #2, #3).
- `data-tables`: boolean cells carry the runtime's `boolean-true|false|null` marker as `data-testid`; the grid and its loading overlay carry `table::<id>::grid` and `table::<id>::loading`; always-shown filters carry `table::<id>::fixed-filters` and `table::<id>::fixed-filter::<attributeName>`.
- `action-system`: button roles match the runtime (`openaddselector` maps to `set`; operation buttons are named by the modeled operation); one `confirmation` identity.

The grammar is owned by `judo-frontend-runtime` capability `testid-contract` (PR A). These requirements mirror it, and task 3.3 checks parity (review #10).

## Non-goals

- New test-id coverage beyond what the branch has.
- Changing the canonical grammar; `@judo/test-ids` (runtime PR A) owns it.

## Impact

- Branch `feature/JNG-6411_Unify_data_testId_contract` of `judo-ui-react-template`: rewritten history, force-push.
- Tatami #287 resolves this template in CI from its published feature build (`react-template-e2e-parity` task 7.2b), so republish after the rebase and update that coordinate. Locally Tatami keeps `1.0.0-SNAPSHOT`.
- Acceptance is not this repo's unit or snapshot tests alone: the Tatami suite must pass on the React stack with the same counts as on the runtime stack (`react-template-e2e-parity` 6.1–6.8). Every one-sided failure is a bug in one of the two engines.
- Test coverage: the itest snapshots (`5d6cc1e4`) are regenerated after the rebase. The Java generator tests for the dropped storage feature are deleted with it.
