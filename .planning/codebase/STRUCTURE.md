# Structure

## Top-Level Layout

```
judo-ui-react-template/
├── pom.xml                              # Parent POM (modules, dependency management)
├── AGENTS.md / CLAUDE.md                # AI agent instructions
├── judo-ui-react/                       # Core generator module
├── judo-ui-react-itest/                 # Integration test modules
├── judo-diff-checker-maven-plugin/      # Snapshot diff-checking Maven plugin
├── docs/                                # Documentation module
├── .github/                             # GitHub Actions CI
├── .nodejs/                             # Shared Node.js installation directory
├── full-build-parallel.sh               # Parallel build script (mvnd)
├── cleanup-itest-targets.sh             # Clean itest target dirs
├── update-test-models.sh               # Update test model files
└── count-lines.sh                       # LOC counting utility
```

## Core Generator Module (`judo-ui-react/`)

```
judo-ui-react/
├── pom.xml                              # Bundle packaging, dependencies
└── src/
    ├── main/
    │   ├── java/hu/blackbelt/judo/ui/generator/react/
    │   │   ├── UiPageContainerHelper.java    # 769 lines — container logic
    │   │   ├── UiWidgetHelper.java           # 623 lines — widget rendering
    │   │   ├── UiPageHelper.java             # 612 lines — page routing/navigation
    │   │   ├── UiActionsHelper.java          # 577 lines — action generation
    │   │   ├── UiTableHelper.java            # 428 lines — table/grid logic
    │   │   ├── UiI18NHelper.java             # 415 lines — i18n key generation
    │   │   ├── UiGeneralHelper.java          # 239 lines — naming utilities
    │   │   ├── UiImportHelper.java           # 164 lines — TS import management
    │   │   ├── UiPandinoHelper.java          # 143 lines — DI hook generation
    │   │   ├── ReactStoredVariableHelper.java # 118 lines — template parameters
    │   │   ├── UIMenuHelper.java             # 90 lines — menu structure
    │   │   ├── UiNPMHelper.java              # 44 lines — npm helpers
    │   │   ├── UiSecurityHelper.java         # 33 lines — security helpers
    │   │   └── mask/
    │   │       └── MaskEntry.java            # 73 lines — mask serialization
    │   └── resources/
    │       ├── ui-react.yaml                 # Template registry (1170 lines)
    │       └── actor/                        # 321 Handlebars templates + 25 static files
    │           ├── package.json.hbs          # Root package config
    │           ├── biome.json.hbs            # Linter config
    │           ├── src/
    │           │   ├── App.tsx.hbs           # App shell
    │           │   ├── auth/                 # Authentication components
    │           │   ├── components/           # Shared UI components
    │           │   │   ├── dialog/           # Dialog components
    │           │   │   ├── table/            # Table components (EagerTable, LazyTable)
    │           │   │   └── widgets/          # Input widgets (SingleRelationInput, Tags, etc.)
    │           │   ├── components-api/       # Component type definitions
    │           │   ├── containers/           # Page container templates (factory)
    │           │   │   ├── container.tsx.hbs # Container component
    │           │   │   ├── page.tsx.hbs      # Page container wrapper
    │           │   │   ├── dialog.tsx.hbs    # Dialog container wrapper
    │           │   │   ├── components/       # Sub-components (table, link, tag, cards)
    │           │   │   └── widget-fragments/ # Widget-specific fragment templates
    │           │   ├── custom/              # Custom implementation hooks
    │           │   ├── dialogs/             # Dialog page templates (factory)
    │           │   ├── fragments/           # Shared template fragments
    │           │   │   ├── container/       # Container fragments
    │           │   │   ├── operations/      # Operation fragments
    │           │   │   ├── page/            # Page fragments
    │           │   │   ├── relation/        # Relation fragments
    │           │   │   └── table/           # Table fragments
    │           │   ├── hooks/               # React hooks
    │           │   ├── l10n/                # Localization
    │           │   ├── layout/              # App layout (Drawer, Header, Footer, BottomMenu)
    │           │   ├── pages/               # Page templates (factory)
    │           │   │   └── actions/         # Action fragment templates
    │           │   ├── theme/               # MUI theme configuration
    │           │   └── utilities/           # Utility functions
    │           └── public/                  # Static assets (icons, i18n JSON)
    └── test/
        └── java/.../MaskEntryTest.java      # Single unit test (48 lines)
```

## Integration Tests (`judo-ui-react-itest/`)

```
judo-ui-react-itest/
├── pom.xml                              # Parent POM for all itests
├── ActionGroupTest/                     # Tests action group rendering
│   ├── model/ActionGroupTest-ui.model   # EMF model input
│   └── action_group_test__god/          # Actor sub-module
│       ├── pom.xml                      # Generator config + snapshot list
│       └── src/test/resources/snapshots/ # Snapshot files for diff checking
├── ActionGroupTestPro/                  # Same as above, MUI Pro variant
├── CRUDActionsTest/                     # Tests CRUD operations
│   ├── crudactions_test__actors__actor/
│   ├── crudactions_test__actors__collections__collection_dashboard_actor/
│   └── crudactions_test__actors__singles__single_dashboard_actor/
├── OperationParametersTest/             # Tests operation parameters
├── RelationTest/                        # Tests relation handling
└── SimpleOrderManagement/              # E2E order management scenario
    ├── simple_order_management__customer/
    └── simple_order_management__registration/
```

## Diff Checker Plugin (`judo-diff-checker-maven-plugin/`)

```
judo-diff-checker-maven-plugin/
├── pom.xml
└── src/main/java/.../DiffCheckerMojo.java  # 107 lines — compares generated vs snapshot files
```

## Naming Conventions
- **Java helpers**: `Ui*Helper.java` — `@TemplateHelper` annotated, static methods
- **Templates**: Mirror generated app paths, `.hbs` suffix (e.g., `src/pages/index.tsx.hbs` → `src/pages/{pagePath}/index.tsx`)
- **Fragment templates**: Partial templates for inclusion, `.fragment.hbs` or nested in `fragments/` directory
- **itest modules**: `{test_name}__{actor_name}` (double underscore separates test from actor)
- **Model files**: `{TestName}-ui.model`
- **Generated paths**: SpEL expressions in `ui-react.yaml` compute output paths from model element names

## Key File Locations
- Template registry: `judo-ui-react/src/main/resources/ui-react.yaml`
- Largest templates: `LazyTable.tsx.hbs` (866 lines), `EagerTable.tsx.hbs` (689 lines), `filter-helper.ts.hbs` (652 lines)
- Largest helpers: `UiPageContainerHelper.java` (769 lines), `UiWidgetHelper.java` (623 lines)
- Generated output: `target/frontend-react/` in each itest actor module
- Snapshots: `src/test/resources/snapshots/frontend-react/` in each itest actor module
