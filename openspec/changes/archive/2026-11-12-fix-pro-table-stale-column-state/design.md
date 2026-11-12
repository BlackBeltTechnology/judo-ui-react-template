# Design

## Context

Both table templates build `cols` inside the `effectiveTableColumns` `useMemo`: the model columns,
followed by the action column from `columnsActionCalculator`, typed `GridColDef<T>[]`. On Pro,
`onColumnsChanged` reads `gridColumnDefinitionsSelector`, drops `__check__`, and stores
`{ field, index, width: computedWidth }[]` (`PersistedColumnInfo`, `actor/src/utilities/table.ts.hbs`)
in React state and in `sessionStorage` via `useDataStore('sessionStorage')`. The restore block
maps saved entries to `cols` with a non-null assertion. See proposal.md for the failure modes.

## Goals / Non-Goals

**Goals:**
- Restoring a layout never crashes on a stale or partial saved state.
- Columns added to the model after a save are visible.
- Saved order and widths of still-existing columns are kept.
- Only the Pro-gated block changes; community output is byte-identical.

**Non-Goals:**
- Changing storage (`sessionStorage` → `localStorage`). The same hook also stores filters, sort and
  page size; switching it would change persistence for all of them.
- Versioning or clearing saved layouts on model change.
- Restoring layout for the Premium plan or for cards/tag containers.
- Running the generated vitest specs in the itest (see Risks).

## Decisions

1. **Restore algorithm.** The algorithm is
   `known = [...columnState].sort(byIndex).map(findInCols).filter(isDefined)`. It then applies the
   saved widths to `known` and returns `[...known, ...cols.filter(c => !known.includes(c))]`.
   - **Copying before sorting** keeps React state immutable.
   - **Dropping unknown fields** is the minimal, lossless fix for removed columns.
   - **Appending from `cols`** keeps model order for new columns and needs no index arithmetic.

   *Alternative:* interleave new columns at their model index. Rejected: the saved indices and the
   model indices are different coordinate systems, so there is no single right position for a new
   column. Appending keeps the result predictable.
2. **Inline in both templates, no shared helper.**
   - The block is ~12 lines and Pro-gated inside each template's `useMemo`.
   - Keeping it inline puts the full logic in both itest snapshots, so the diff checker covers it.
   - `utilities/table.ts` has no Pro snapshot, so a helper there would be checked only by the
     type-check/Vite build.
   - This is an accepted exception to the "extract repeated patterns" rule; the user chose it on
     2026-11-10.
3. **Type guard uses the component generic `T`.** Both templates type `cols` as `GridColDef<T>[]`,
   so the filter is `(c): c is GridColDef<T> => !!c`. This keeps `tsgo` strict-clean without the
   old `!`.
4. **Widths are still mutated in place** on the `GridColDef` objects in `cols`. `cols` is rebuilt on
   every memo run, so this does not leak into column definitions shared elsewhere. This matches the
   existing behaviour.

## Risks / Trade-offs

- [The restore logic has no runtime unit test] → The itest checks the generated text (snapshot),
  formatting (Biome) and types (`tsgo`), but not behaviour. The generated app has vitest specs
  (`utilities/*.test.ts`), yet the itest POM runs only `install`, `format`, `build`. Behaviour is
  covered downstream by the rackinspect E2E specs (its tasks 3.10/3.11).
- [Pro coverage was thin] → `ActionGroupTestPro` is the only Pro itest. Before this change, nothing
  snapshot-checked the license-key wiring and no Java test covered plan resolution. Fixed by task
  group 3:
  - `src/main.tsx` is added to the diff checker;
  - `ReactStoredVariableHelperTest` is added. A mutation check (making the `pro` match
    case-sensitive) fails 2 of its 8 tests.
- [Which license files to snapshot] → Only `src/main.tsx`. Without the `setLicenseKey` line the
  build still passes and the app only shows a watermark at runtime, so the snapshot is the only
  check that catches its removal.
  - `package.json` is not snapshotted. Missing `-pro` dependencies already fail `tsgo`/Vite
    (unresolved imports), and most recent changes to the dependency templates are version bumps,
    each of which would need a snapshot refresh.
  - `src/vite-env.d.ts` is not snapshotted. Vite's `ImportMetaEnv` has an `any` fallback, so
    losing the `VITE_MUI_LICENSE_KEY` typing breaks nothing.
- [The order of a new column relative to saved ones is "after", not "where the model puts it"] →
  The user can drag it once, and the new order is saved.
