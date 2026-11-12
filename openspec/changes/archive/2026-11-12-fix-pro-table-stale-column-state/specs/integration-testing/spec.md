## MODIFIED Requirements

### Requirement: MUI Pro tests SHALL use pro license plan

The `ActionGroupTestPro` test suite SHALL generate code with `muiLicensePlan=pro` to verify MUI Pro/Premium feature generation.

#### Scenario: Pro license generation
- **GIVEN** the `ActionGroupTestPro` POM sets `<muiLicensePlan>pro</muiLicensePlan>`
- **WHEN** the code generator runs
- **THEN** the generated app imports MUI Pro components (e.g., `DataGridPro`) and includes license setup code

#### Scenario: License key wiring is snapshot-checked
- **GIVEN** the `ActionGroupTestPro` diff checker lists `src/main.tsx` as a source
- **WHEN** the integration test builds
- **THEN** removing or changing the `@mui/x-license` import or the `LicenseInfo.setLicenseKey(import.meta.env.VITE_MUI_LICENSE_KEY)` call fails `checkDiffs` until the snapshot is updated

## ADDED Requirements

### Requirement: MUI license plan resolution SHALL be unit-tested

`ReactStoredVariableHelper` plan resolution SHALL be covered by JUnit tests in `judo-ui-react`. The tests SHALL check how the `muiLicensePlan` template variable maps to the plan, the DataGrid component, and the package suffixes.

#### Scenario: Pro plan
- **WHEN** `muiLicensePlan` is `pro` (any letter case)
- **THEN** `getMUILicensePlan()` is `pro`, `isMUILicensed()` and `isMUILicensePlanPro()` are true, `muiDataGridComponent()` is `DataGridPro`, and both plan suffixes are `-pro`

#### Scenario: Premium plan
- **WHEN** `muiLicensePlan` is `premium`
- **THEN** `isMUILicensed()` is true, `isMUILicensePlanPro()` is false, `muiDataGridComponent()` is `DataGridPremium`, the DataGrid suffix is `-premium` and the pickers suffix is `-pro`

#### Scenario: Community or unset plan
- **WHEN** `muiLicensePlan` is `community`, unset, or an unknown value
- **THEN** `getMUILicensePlan()` is empty, `isMUILicensed()` is false, `muiDataGridComponent()` is `DataGrid`, both suffixes are empty, and `isUseInlineColumnFilters()` is false even when `useInlineColumnFilters` is `true`
