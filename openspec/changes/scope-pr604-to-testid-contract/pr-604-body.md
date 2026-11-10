# JNG-6411 React template: the test-id contract

The goal of JNG-6411 is that one Tatami suite runs unchanged against both frontends: the app this template generates and `judo-frontend-runtime`. For that, both have to emit the same `data-testid` values. This PR makes the template emit the grammar of `@judo/test-ids`, the runtime's test-id package. It is the React counterpart of runtime [#78](https://github.com/BlackBeltTechnology/judo-frontend-runtime/pull/78) (PR A), and like #78 it **only changes test ids**.

The behaviour fixes we found while migrating the suite are not here. They are on `feature/react-followups`, the counterpart of runtime [#80](https://github.com/BlackBeltTechnology/judo-frontend-runtime/pull/80) (PR B).

## Behaviour changes

None visible to users. Two things change under the hood:

- **Row identity (`1606a585`):** every place that keyed a row by `row.__identifier!` now goes through `resolveTransferId(row)`, the same lookup order the runtime uses (`__signedIdentifier`, `__identifier`, `__tempId`, …). A new unsaved row gets `__tempId` and `__isNew: true` instead of a `draft:`-prefixed `__identifier`. Saved data and the requests sent to the backend do not change.
- **Old test ids are gone.** Specs that used the ad-hoc ids (`*-dropdown-toggle`, `*-clear-all`, `use-crud-dialog-execute`, `::button::<name>` on row actions, …) have to switch to the canonical ones. The only consumer is the Tatami suite, which already uses the new ids.

## What the ids look like

| Element | Test id |
|---|---|
| Field, its input and its buttons | `field::<id>`, `field::<id>::input`, `field::<id>::button::set` |
| Enumeration option | `field::<id>::option::<member>` |
| Table, grid, loading overlay | `table::<id>`, `table::<id>::grid`, `table::<id>::loading` |
| Row, cell, selection cell | `table::<id>::row::<rid>`, `…::cell::<column>`, `…::cell::__check__` |
| Row action | `table::<id>::row::<rid>::action::<role>` (an operation uses its modeled name) |
| Filter dialog | `table::<id>::filter-panel`, rows `…::row::<index>` with `::column`, `::operator`, `::value` |
| Always-shown filters | `table::<id>::fixed-filters`, `table::<id>::fixed-filter::<attribute>` |
| Dialogs, tabs, drawer, breadcrumb | `dialog::confirmation::confirm`, `tabs::<ctrl>::<tab>`, `nav::item::<path>`, `breadcrumb::<index>` |
| Boolean cell | `boolean-true`, `boolean-false`, `boolean-null` |

`<id>` is the model element's `sourceId` when it has one, otherwise its `xmi:id`, as on the runtime. The full grammar is in `docs/DATA_IDENTITY.md` and in the `transfer-identity` spec.

## Taken out of the original branch

The review on runtime #69 asked to keep the test-id work free of UI changes, so the same split is done here:

- **Storage key namespacing** (`19d9281b`, `d188686f`, `4ebc5b35`) is dropped. One deployment serves one frontend, so the two engines never share browser storage. The hunks that leaked into later commits are gone too.
- **Confirmations:** the confirmation before clear and before an eager bulk delete (from `0c23309c`) moved to `feature/react-followups`. Only the shared `confirmation` dialog id stays here.
- **Dates:** `f4c258c0` forced date pickers into a single plain input. MUI X v9 no longer allows that, so only its checkbox half is here. `sectioned-date-fields` on `feature/react-followups` replaces the date half, together with the runtime and Tatami changes of the same name.

The OpenSpec changes the branch had already delivered (`unify-data-id-and-testid-with-runtime`, `align-testids-with-runtime-package`, `align-domain-testids-with-runtime`) are archived, and their specs merged into `openspec/specs`.

## Verification

- `mvn clean install -DskipPrepareNodeJS -DforceSnapshotOverwrite=true` (regenerates the itest snapshots), then `mvn clean install -DskipPrepareNodeJS` (checks them): both green, all 9 generated itest apps, 62 generator tests, no snapshot drift on the second pass.
- Acceptance is the Tatami run on the React stack, with the same counts as on the runtime stack (`judo-tatami-tests` `react-template-e2e-parity`). That run needs `feature/react-followups` as well, because the date specs depend on it.

Rebased on `develop` (`4333ae63`), including JNG-6410 (autocomplete no-results hint); `SingleRelationInput`, `Tags` and `TextWithTypeAhead` keep both the hint and the new ids.
