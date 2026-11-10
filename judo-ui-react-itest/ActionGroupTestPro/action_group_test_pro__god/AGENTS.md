# ActionGroupTestPro / action_group_test_pro__god — Pro-plan itest (God actor)

Only itest module generating with `muiLicensePlan=pro` (set in parent `ActionGroupTestPro/pom.xml`).
Pipeline: generate → `checkDiffs` → `pnpm install` → `pnpm run format` → `pnpm run build` (`tsgo && vite build`).
Rows cover edited files only.

| File | Purpose |
|---|---|
| `pom.xml` | `judo-diff-checker-maven-plugin` `<sources>`: 2 pages, `ViewGalaxyTableTableComponent`, `LazyTable.tsx`, `EagerTable.tsx`, plus license-key wiring `src/main.tsx` (`LicenseInfo.setLicenseKey(import.meta.env.VITE_MUI_LICENSE_KEY)`). `package.json`/`vite-env.d.ts` deliberately not snapshotted (build catches missing deps; churn). Snapshots in `src/test/resources/snapshots/frontend-react/<path>.snapshot`; refresh by copying from `target/frontend-react/`. See change: JNG-6433 |
