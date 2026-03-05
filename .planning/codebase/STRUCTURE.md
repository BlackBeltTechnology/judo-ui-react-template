# Structure

## Top-Level Directory Layout

```
judo-ui-react-template/
├── pom.xml                              # Parent POM (Java 21, version management)
├── CLAUDE.md / AGENTS.md                # AI agent instructions
├── full-build-parallel.sh               # Parallel build via mvnd
├── update-test-models.sh                # Model update utility
├── cleanup-itest-targets.sh             # Clean itest outputs
├── count-lines.sh                       # Code metrics
├── logback-test.xml                     # Test logging config
│
├── judo-ui-react/                       # Core generator module
├── judo-ui-react-itest/                 # Integration test modules
├── judo-diff-checker-maven-plugin/      # Snapshot diff testing plugin
├── docs/                                # Documentation module
├── .mvn/                                # Maven wrapper
└── .nodejs/                             # Auto-installed Node.js
```

## Core Generator (`judo-ui-react/`)

```
judo-ui-react/
├── pom.xml                              # Bundle packaging, EMF + SpEL deps
└── src/main/
    ├── java/hu/blackbelt/judo/ui/generator/react/
    │   ├── UiPageContainerHelper.java   # 769 lines - Container logic
    │   ├── UiWidgetHelper.java          # 623 lines - Widget rendering
    │   ├── UiPageHelper.java            # 612 lines - Page routing/navigation
    │   ├── UiActionsHelper.java         # 577 lines - Action/button generation
    │   ├── UiTableHelper.java           # 428 lines - Table/grid logic
    │   ├── UiI18NHelper.java            # 415 lines - Translations
    │   ├── UiGeneralHelper.java         # 239 lines - Path/naming utilities
    │   ├── UiImportHelper.java          # 164 lines - MUI import maps
    │   ├── UiPandinoHelper.java         # 143 lines - DI customization
    │   ├── ReactStoredVariableHelper.java # 118 lines - Config vars
    │   ├── UIMenuHelper.java            # 90 lines - Menu generation
    │   ├── UiNPMHelper.java             # 44 lines - NPM naming
    │   ├── UiSecurityHelper.java        # 33 lines - Auth helpers
    │   └── mask/MaskEntry.java          # 74 lines - Query mask serialization
    │
    └── resources/
        ├── ui-react.yaml                # Template registry (1170 lines)
        └── actor/                       # Handlebars templates (321 files)
            ├── package.json.hbs         # Singleton templates
            ├── biome.json.hbs
            ├── public/                  # Static assets + i18n templates
            │   └── i18n/               # Translation JSON templates
            └── src/
                ├── App.tsx.hbs          # App shell
                ├── auth/                # Auth templates (7 files)
                ├── components/          # Shared component templates
                │   ├── dialog/          # Dialog system
                │   ├── table/           # DataGrid (Lazy/Eager)
                │   └── widgets/         # Input widgets
                ├── components-api/      # TypeScript interfaces
                ├── config/              # App config templates
                ├── containers/          # Container templates (factory)
                │   ├── container.tsx.hbs
                │   ├── dialog.tsx.hbs
                │   ├── page.tsx.hbs
                │   ├── components/      # Sub-components (link, table, tag, cards)
                │   └── widget-fragments/ # Per-widget-type fragments
                ├── custom/              # Pandino hook templates
                ├── dialogs/             # Dialog page templates (factory)
                ├── fragments/           # Reusable template fragments
                │   ├── container/       # Container-level fragments
                │   ├── operations/      # Operation handling
                │   ├── page/            # Page-level fragments
                │   ├── relation/        # Relation fragments
                │   └── table/           # Table fragments
                ├── hooks/               # React hook templates
                ├── l10n/                # Localization
                ├── layout/              # App layout (Drawer, Header, Footer)
                ├── pages/               # Page templates (factory)
                │   └── actions/         # Action fragment templates
                ├── theme/               # MUI theme templates
                └── utilities/           # Utility templates
```

## Integration Tests (`judo-ui-react-itest/`)

```
judo-ui-react-itest/
├── pom.xml                              # Parent for all itests
├── ActionGroupTest/                     # Tests button groups (community MUI)
│   ├── model/ActionGroupTest-ui.model   # EMF/XMI input model
│   └── action_group_test__god/          # Actor-specific module
│       ├── pom.xml                      # Generation + build + snapshot config
│       └── src/test/resources/snapshots/ # Committed snapshot files (.snapshot)
├── ActionGroupTestPro/                  # Same test with MUI Pro license
├── CRUDActionsTest/                     # CRUD operations (3 actors)
├── OperationParametersTest/             # Operation parameter handling
├── RelationTest/                        # Relation rendering
└── SimpleOrderManagement/               # E2E order management (2 actors)
```

## Naming Conventions

- **Java classes**: `Ui<Domain>Helper` pattern (e.g., `UiPageHelper`, `UiTableHelper`)
- **Templates**: Mirror generated file paths (e.g., `actor/src/pages/index.tsx.hbs` -> `src/pages/.../index.tsx`)
- **Fragment templates**: `*.fragment.hbs` suffix for included partials
- **Factory-generated files**: Named via SpEL expressions using model element names
- **Itest modules**: `<model_name>__<actor_name>` with double underscore separator
- **Snapshots**: `.snapshot` extension under `src/test/resources/snapshots/frontend-react/`
