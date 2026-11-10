# Tasks

## 1. Test-first: expected Pro snapshots

- [x] 1.1 Edit `ActionGroupTestPro/action_group_test_pro__god/.../snapshots/frontend-react/src/components/table/LazyTable.tsx.snapshot` and `EagerTable.tsx.snapshot` to the target restore block (copy before sorting, `GridColDef<T>` type guard, append missing columns). Verify: `mvn clean install -pl judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god -am` fails in `checkDiffs` on exactly that block (RED).

## 2. Template fix

- [x] 2.1 Rewrite the `{{# if isMUILicensePlanPro }}` restore block in `actor/src/components/table/LazyTable.tsx.hbs` and `EagerTable.tsx.hbs` per design decisions 1–3. Verify: the same Pro itest passes `checkDiffs`, `pnpm run format` (Biome) and `pnpm run build` (`tsgo && vite build`), and the generated files under `target/frontend-react/` match the snapshots byte-for-byte.
- [x] 2.2 Run the community itest `mvn install -pl judo-ui-react-itest/ActionGroupTest/action_group_test__god -am`. Verify: BUILD SUCCESS with no snapshot diff (block is Pro-gated). Confirm no other itest module uses `muiLicensePlan=pro`.
- [x] 2.3 Scaffold `judo-ui-react/src/main/resources/actor/src/components/table/AGENTS.md` with rows for `EagerTable.tsx.hbs` and `LazyTable.tsx.hbs` (`See change: JNG-6433`). Verify: the file exists and the rows describe the storage keys and the restore contract.

## 3. Pro plan and license-key test coverage

- [x] 3.1 Add `judo-ui-react/src/test/java/hu/blackbelt/judo/ui/generator/react/ReactStoredVariableHelperTest.java` (JUnit 5, binds `ThreadLocalContextHolder` per test) for the pro / premium / community / unset / unknown / mixed-case plan scenarios in `specs/integration-testing`. Verify: `mvn test -pl judo-ui-react` runs the new tests and they pass.
- [x] 3.2 Add `src/main.tsx` to the `ActionGroupTestPro/action_group_test_pro__god` `checkDiffs` `<sources>` and commit its snapshot, copied from the Biome-formatted `target/frontend-react/` output. `package.json` and `src/vite-env.d.ts` are deliberately not snapshotted (see design Risks). Verify: the Pro itest passes, and temporarily breaking the snapshot's `setLicenseKey` line makes `checkDiffs` fail.

## 4. Integration and release

- [x] 4.1 Validate this change: `openspec validate fix-pro-table-stale-column-state --strict` passes.
- [ ] 4.2 Commit on `feature/JNG-6433_pro_table_stale_column_state` with a `JNG-6433` message (after user go-ahead), push, and open the PR against `develop`. Verify: CI build of the branch is green.
- [ ] 4.3 Publish a feature-branch build per `.github/CIFLOW.md`. Verify: the `judo-ui-react` artifact version string is recorded in this task line and resolvable from Nexus.
- [ ] 4.4 Downstream check in rackinspect (`enable-mui-x-pro` task 4.2): bump `judo-ui-react-template-version` to the 4.3 build. Verify: its E2E tasks 3.10 and 3.11 (seeded `sessionStorage` `<uniqueId>-columnState`) turn GREEN.

## Workflow follow-up

- Sync the delta spec into `openspec/specs/data-tables/spec.md` and archive this change after the PR is merged.
- Possible follow-up (not in scope): make the itest POMs run `pnpm test` so the generated vitest specs (`src/utilities/*.test.ts`) execute.
