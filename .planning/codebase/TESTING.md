# Testing

## Test Framework

- **JUnit 5** (junit-jupiter 5.5.1) for Java unit tests
- **Snapshot-based regression testing** via `judo-diff-checker-maven-plugin`
- **Frontend build verification** via `pnpm run build` (Vite) + `pnpm run test` (Vitest)

## Integration Test Pipeline

Each itest module follows this Maven lifecycle:

1. **`generate-sources`**: Run `judo-ui-generator-maven-plugin` on `.model` file
   - Phase 1: Generate TypeScript REST layer -> `target/frontend-react/src/services/`
   - Phase 2: Generate React UI -> `target/frontend-react/`
2. **`generate-sources`**: `pnpm install` (dependencies)
3. **`generate-sources`**: `pnpm run format` (Biome formatting)
4. **`generate-sources`**: `judo-diff-checker-maven-plugin:checkDiffs` (snapshot comparison)
5. **`test`**: `pnpm run build` (Vite compilation check)
6. **`test`**: `pnpm run --if-present test` (unit tests if present)

## Snapshot Testing

### How It Works
The `judo-diff-checker-maven-plugin` compares generated files against committed snapshots:
- **Source**: `target/frontend-react/<file>` (freshly generated)
- **Snapshot**: `src/test/resources/snapshots/frontend-react/<file>.snapshot` (committed)
- **Configuration**: Each itest POM lists specific files to check in `<sources>` block

### Example (from ActionGroupTest)
```xml
<configuration>
    <sourceDirectory>${project.basedir}/target/frontend-react/</sourceDirectory>
    <snapshotDirectory>${project.basedir}/src/test/resources/snapshots/frontend-react/</snapshotDirectory>
    <sources>
        <source>src/pages/God/God/Galaxies/AccessViewPage/index.tsx</source>
        <source>src/containers/View/Galaxy/Form/ViewGalaxyForm.tsx</source>
        <!-- ... more files -->
    </sources>
</configuration>
```

### Updating Snapshots
When templates change, copy generated files from `target/frontend-react/` to `src/test/resources/snapshots/frontend-react/` (with `.snapshot` extension? - check plugin behavior).

## Test Modules

| Module | Actors | Tests |
|--------|--------|-------|
| `ActionGroupTest` | god | Button groups, action definitions (community MUI) |
| `ActionGroupTestPro` | god | Same as above with MUI Pro license |
| `CRUDActionsTest` | actor, collection_dashboard_actor, single_dashboard_actor | CRUD operations |
| `OperationParametersTest` | actor | Operation parameter handling |
| `RelationTest` | actor | Relation rendering (inline edit, tags, etc.) |
| `SimpleOrderManagement` | customer, registration | E2E order management flow |

## Generated App Tests

Some generated utility files include tests that are copied as-is (not templated):
- `actor/src/utilities/filter-helper.test.ts` (copy: true)
- `actor/src/utilities/helper.test.ts` (copy: true)
- `actor/src/utilities/table.test.ts` (copy: true)

These run via `pnpm run --if-present test` during the `test` phase.

## Test Model Files

Each test suite has an EMF/XMI model file:
- `judo-ui-react-itest/<TestName>/model/<TestName>-ui.model`
- 6 model files total across all test suites
- Models define pages, containers, actions, relations for testing specific template behaviors
