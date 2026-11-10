# actor/src/components/table — table component templates

Handlebars templates for generated MUI DataGrid table components. Rows below cover
edited files only; add a row when touching any other file here.

| File | Purpose |
|---|---|
| `EagerTable.tsx.hbs` | Client-side (eager) table. Persists `filterModel`/`filters`/`rowsPerPage`/`sortModel` and, Pro-only, `columnState` (`PersistedColumnInfo[]`) via `useDataStore('sessionStorage')` under `${uniqueId}-*` keys. Pro `effectiveTableColumns` restore: sort copy of `columnState` by `index`, drop saved fields absent from `cols`, apply saved widths, append unsaved `cols` in model order. Snapshot: `ActionGroupTestPro/action_group_test_pro__god`. See change: JNG-6433 |
| `LazyTable.tsx.hbs` | Server-paged (lazy) table. Same storage keys/hook and same Pro column-state restore as `EagerTable.tsx.hbs`; layout saved in `onColumnsChanged` (`gridColumnDefinitionsSelector`, `__check__` excluded). Snapshot: `ActionGroupTestPro/action_group_test_pro__god`. See change: JNG-6433 |
