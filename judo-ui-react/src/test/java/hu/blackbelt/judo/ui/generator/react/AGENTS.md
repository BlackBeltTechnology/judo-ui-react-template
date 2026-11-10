# judo-ui-react unit tests — hu.blackbelt.judo.ui.generator.react

JUnit 5 tests for generator helpers. Run: `mvn test -pl judo-ui-react`. Rows cover edited files only.

| File | Purpose |
|---|---|
| `ReactStoredVariableHelperTest.java` | Pins MUI plan resolution from `muiLicensePlan` via `ThreadLocalContextHolder.bindContext` (unbinds `@AfterEach`). Covers pro (any case) / premium / community / empty / unknown / unset → `getMUILicensePlan`, `isMUILicensed`, `isMUILicensePlanPro`, `muiDataGridComponent`, `muiDataGridPropsType`, DataGrid + pickers suffixes, `isUseInlineColumnFilters` gated by license. See change: JNG-6433 |
