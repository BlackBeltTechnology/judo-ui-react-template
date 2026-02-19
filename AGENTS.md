# JUDO UI React Template - AI Agent Instructions

This document provides comprehensive guidance for AI coding assistants working on the JUDO UI React Template project.

## Project Overview

**JUDO UI React Template** is a code generation framework that transforms JUDO UI meta-models into fully functional React TypeScript applications. It uses Handlebars templates with SpringEL expressions to generate complete frontend applications.

### Key Facts
- **Language**: Java 21 (generator), TypeScript (generated apps)
- **Build Tool**: Maven 3.9.4
- **Packaging**: OSGi Bundle
- **Template Engine**: Handlebars 4.4.0
- **Expression Language**: Spring Expression (SpEL)
- **Frontend Stack**: React 19, Material-UI (MUI), Vite, pnpm
- **Repository**: https://github.com/BlackBeltTechnology/judo-ui-react-template
- **License**: Eclipse Public License 2.0

### What This Project Does
1. Takes a JUDO UI model (`.model` file in EMF/Ecore XMI format)
2. Processes 300+ Handlebars templates
3. Generates a complete React TypeScript application with:
   - Pages, dialogs, and containers
   - CRUD operations and data tables
   - Authentication and authorization
   - Internationalization (i18n)
   - Material-UI theming
   - REST API integration

## Directory Structure

```
judo-ui-react-template/
├── judo-ui-react/                    # Main generator module
│   ├── pom.xml                       # Maven configuration
│   └── src/
│       ├── main/
│       │   ├── java/hu/blackbelt/judo/ui/generator/react/
│       │   │   ├── UiGeneralHelper.java      # String/element utilities
│       │   │   ├── UiWidgetHelper.java       # Widget template resolution
│       │   │   ├── UiPageHelper.java         # Page routing/classification
│       │   │   ├── UiPageContainerHelper.java # Container naming/actions
│       │   │   ├── UiActionsHelper.java      # Action definitions
│       │   │   ├── UiTableHelper.java        # Table column/filter config
│       │   │   ├── UiI18NHelper.java         # Translation key generation
│       │   │   ├── UiImportHelper.java       # MUI/API import collection
│       │   │   ├── UiPandinoHelper.java      # Custom implementation detection
│       │   │   ├── UIMenuHelper.java         # Menu operations
│       │   │   ├── UiNPMHelper.java          # NPM package naming
│       │   │   ├── UiSecurityHelper.java     # Client ID generation
│       │   │   └── ReactStoredVariableHelper.java # ThreadLocal context
│       │   └── resources/
│       │       ├── ui-react.yaml             # Template descriptor (300+ templates)
│       │       └── actor/                    # Handlebars templates (321 files)
│       │           ├── package.json.hbs
│       │           ├── vite.config.ts.hbs
│       │           ├── index.html.hbs
│       │           └── src/
│       │               ├── App.tsx.hbs
│       │               ├── main.tsx.hbs
│       │               ├── routes.tsx.hbs
│       │               ├── hooks/            # React hooks
│       │               ├── auth/             # Authentication
│       │               ├── components/       # UI components
│       │               ├── containers/       # Page containers
│       │               ├── pages/            # Page templates
│       │               ├── dialogs/          # Dialog templates
│       │               ├── layout/           # Layout components
│       │               ├── theme/            # MUI theme
│       │               ├── custom/           # Customization hooks
│       │               ├── utilities/        # Helper utilities
│       │               └── l10n/             # Localization
│       └── test/                             # Unit tests
│
├── judo-ui-react-itest/              # Integration tests
│   ├── ActionGroupTest/              # Action groups test
│   ├── CRUDActionsTest/              # CRUD operations test
│   ├── OperationParametersTest/      # Operation parameters test
│   ├── RelationTest/                 # Relations test
│   └── SimpleOrderManagement/        # Complete app example
│
├── judo-diff-checker-maven-plugin/   # Diff checking utility
├── docs/                             # Project documentation
├── pom.xml                           # Parent Maven POM
├── AGENTS.md                         # This file
├── CLAUDE.md                         # Claude instructions
└── full-build-parallel.sh            # Build script
```

## Build Commands

### Quick Reference

```bash
# Full build (recommended)
./full-build-parallel.sh

# Standard Maven build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl judo-ui-react

# Run with Maven daemon (faster)
mvnd clean install
```

### Build Requirements
- Java 21
- Maven 3.9.4+
- Node.js 22.11.0 (auto-installed via Maven)
- pnpm 9.12.3 (auto-installed via Maven)

## Template System

### How Templates Work

Templates are defined in `ui-react.yaml` and processed by the generator:

```yaml
templates:
  - name: actor/src/pages/index.tsx
    factoryExpression: "#getPagesForRouting(#application)"
    pathExpression: "'src/pages/' + #pagePath(#self) + '/index.tsx'"
    templateName: actor/src/pages/index.tsx.hbs
    templateContext:
      - name: page
        expression: "#self"
```

**Key Properties:**
- `name`: Template identifier (for overrides)
- `pathExpression`: SpringEL expression for output file path
- `templateName`: Handlebars template file path
- `factoryExpression`: Returns collection to iterate over
- `conditionExpression`: Boolean; skip if false
- `templateContext`: Additional variables for template

### Expression Variables

| Variable | When Available | Description |
|----------|---------------|-------------|
| `#self` | Always | Current context element |
| `#model` | Always | The Application object |
| `#application` | When app context available | Application object |
| `#actorType` | Actor-based templates | Current actor type |
| `#container` | Container templates | Current page container |
| `#page` | Page templates | Current page definition |

### Helper Classes

All helpers are in `hu.blackbelt.judo.ui.generator.react`:

| Class | Purpose | Key Methods |
|-------|---------|-------------|
| `UiGeneralHelper` | String/element utilities | `pathName()`, `safeName()`, `createId()` |
| `UiWidgetHelper` | Widget resolution | `getWidgetTemplate()`, `calculateSize()` |
| `UiPageHelper` | Page routing | `getPagesForRouting()`, `getPageRoute()` |
| `UiPageContainerHelper` | Container handling | `containerPath()`, `containerComponentName()` |
| `UiActionsHelper` | Action definitions | `getContainerOwnActionDefinitions()` |
| `UiTableHelper` | Table configuration | `getFilterTypeForAttribute()`, `columnWidth()` |
| `UiI18NHelper` | Translations | `i18nMenuTreeLabels()`, `i18nEnumerationTypes()` |
| `UiImportHelper` | Import collection | `getMaterialImportsForPageContainer()` |

## Key Template Files

### Component Templates

| Template | Generated Output | Purpose |
|----------|-----------------|---------|
| `App.tsx.hbs` | `src/App.tsx` | Root application component |
| `routes.tsx.hbs` | `src/routes.tsx` | React Router configuration |
| `main.tsx.hbs` | `src/main.tsx` | Application entry point |

### Layout Templates

| Template | Purpose |
|----------|---------|
| `layout/Header.tsx.hbs` | Application header with menus |
| `layout/Drawer.tsx.hbs` | Navigation drawer |
| `layout/Footer.tsx.hbs` | Application footer |
| `layout/Navigator.tsx.hbs` | Menu navigation |

### Component Templates

| Template | Purpose |
|----------|---------|
| `components/table/LazyTable.tsx.hbs` | Data grid with lazy loading |
| `components/table/EagerTable.tsx.hbs` | Data grid with eager loading |
| `components/table/FixedTableFilters.tsx.hbs` | Collapsible filter panel |
| `components/dialog/*.hbs` | Dialog components |
| `components/widgets/*.hbs` | Form input widgets |

### Page Templates

| Template | Purpose |
|----------|---------|
| `pages/index.tsx.hbs` | Page component |
| `containers/**/index.tsx.hbs` | Page container |
| `dialogs/**/index.tsx.hbs` | Dialog component |

## Generated Application Structure

The generator produces a React application with this structure:

```
target/frontend-react/
├── package.json                 # Dependencies and scripts
├── vite.config.ts              # Vite build configuration
├── tsconfig.json               # TypeScript configuration
├── biome.json                  # Linting configuration
├── index.html                  # HTML entry point
├── public/
│   ├── i18n/                   # Translation files
│   │   ├── application_default.json
│   │   ├── system_en-US.json
│   │   └── system_hu-HU.json
│   └── manifest.json
└── src/
    ├── App.tsx                 # Root component
    ├── main.tsx                # Entry point
    ├── routes.tsx              # Routing configuration
    ├── auth/                   # Authentication
    ├── components/             # Reusable components
    │   ├── dialog/            # Dialog components
    │   ├── table/             # Table components
    │   └── widgets/           # Form widgets
    ├── containers/             # Page containers
    ├── pages/                  # Page components
    ├── dialogs/                # Dialog pages
    ├── layout/                 # Layout components
    ├── hooks/                  # React hooks
    ├── theme/                  # MUI theme
    ├── utilities/              # Helper functions
    ├── l10n/                   # Localization hooks
    └── custom/                 # Customization points
```

## React Component Architecture

### State Management

The generated app uses Context-based state management:

```typescript
// Global contexts
ConfigContext        // App configuration
EventBusContext      // Cross-component events
PrincipalContext     // Logged-in user data
ViewContext          // Page/dialog view models

// Hooks
useConfig()          // Access configuration
usePrincipal()       // Access user data
useViewContext()     // Access view model
useSnacks()          // Toast notifications
```

### Data Flow

```
API Service (Axios)
    ↓
Custom Hook (useDataStore, useCRUDDialog)
    ↓
Context Provider
    ↓
Components (via useContext)
```

### Key Components

#### FixedTableFilters
Collapsible filter panel for data tables.

**Location**: `components/table/FixedTableFilters.tsx.hbs`

**Props**:
- `tableFilterOptions`: Available filters
- `filters`: Current active filters
- `onFiltersChange`: Change handler
- `collapsible`: Enable collapse (default: true)
- `maxFirstRowCols`: Max columns in first row (default: 12)

**Features**:
- Automatic filter overflow to collapsible section
- Magnifier icon indicator
- Expand/collapse toggle

#### LazyTable
Data grid with server-side pagination and filtering.

**Location**: `components/table/LazyTable.tsx.hbs`

**Props**:
- `columns`: Column definitions
- `dataSource`: Data fetching function
- `filters`: Filter state
- `onFiltersChange`: Filter change handler
- `collapsibleFilters`: Enable collapsible filters

#### SingleValueFilterComponent
Individual filter input component.

**Location**: `components/table/SingleValueFilterComponent.tsx.hbs`

**Filter Types**:
- Text (string)
- Numeric (number)
- Date/DateTime/Time
- Enumeration (dropdown)
- Boolean (checkbox)

## Integration Tests

### Test Projects

| Test | Purpose | Location |
|------|---------|----------|
| `ActionGroupTest` | Basic action groups | `judo-ui-react-itest/ActionGroupTest/` |
| `ActionGroupTestPro` | MUI Pro features | `judo-ui-react-itest/ActionGroupTestPro/` |
| `CRUDActionsTest` | CRUD operations | `judo-ui-react-itest/CRUDActionsTest/` |
| `OperationParametersTest` | Operation params | `judo-ui-react-itest/OperationParametersTest/` |
| `RelationTest` | Relations/links | `judo-ui-react-itest/RelationTest/` |
| `SimpleOrderManagement` | Complete example | `judo-ui-react-itest/SimpleOrderManagement/` |

### Running Tests

```bash
# Run all integration tests
cd judo-ui-react-itest
mvn clean install

# Run specific test
cd judo-ui-react-itest/CRUDActionsTest
mvn clean install
```

## Snapshot Testing

### CRITICAL: Update Snapshots After Every Template or Helper Change

The `judo-diff-checker-maven-plugin` compares generated output files against committed snapshots. **After every change to templates (`.hbs` files) or Java helper classes, you MUST update all affected snapshots.** The build will fail if any snapshot does not match the newly generated output.

### How It Works

1. Each itest sub-project's `pom.xml` configures `judo-diff-checker-maven-plugin` with a list of source files to check.
2. During `mvn install`, the plugin compares files in `target/frontend-react/` against their corresponding `.snapshot` files in `src/test/resources/snapshots/frontend-react/`.
3. If any diff is detected, the build fails with an error like:
   ```
   Snapshot changes detected in .../target/frontend-react/src/components/table/EagerTable.tsx
   ```

### Snapshot File Locations

Snapshots live alongside each itest sub-module:

```
judo-ui-react-itest/<TestProject>/<actor_module>/
├── src/test/resources/snapshots/frontend-react/   # Committed snapshots
│   └── src/components/table/EagerTable.tsx.snapshot
└── target/frontend-react/                          # Generated output (build artifact)
    └── src/components/table/EagerTable.tsx
```

The snapshot file is the generated file with a `.snapshot` suffix appended.

### How to Update Snapshots

**Step 1**: Build the itest projects to generate the latest output:

```bash
cd judo-ui-react-itest
mvn clean install -DskipDiffCheck   # or build until generation completes
```

**Step 2**: Find mismatched snapshots:

```bash
# From the repository root
find judo-ui-react-itest -path '*/src/test/resources/snapshots/*' -name '*.snapshot' | while read snapshot; do
  project_dir=$(echo "$snapshot" | sed 's|/src/test/resources/snapshots/frontend-react/.*||')
  relative_path=$(echo "$snapshot" | sed 's|.*/src/test/resources/snapshots/frontend-react/||' | sed 's|\.snapshot$||')
  generated="$project_dir/target/frontend-react/$relative_path"
  if [ -f "$generated" ] && ! diff -q "$snapshot" "$generated" > /dev/null 2>&1; then
    echo "MISMATCH: $snapshot"
  fi
done
```

**Step 3**: Copy the generated files over the outdated snapshots:

```bash
# For a single file
cp <project>/target/frontend-react/path/to/File.tsx \
   <project>/src/test/resources/snapshots/frontend-react/path/to/File.tsx.snapshot

# Or update all mismatched snapshots at once
find judo-ui-react-itest -path '*/src/test/resources/snapshots/*' -name '*.snapshot' | while read snapshot; do
  project_dir=$(echo "$snapshot" | sed 's|/src/test/resources/snapshots/frontend-react/.*||')
  relative_path=$(echo "$snapshot" | sed 's|.*/src/test/resources/snapshots/frontend-react/||' | sed 's|\.snapshot$||')
  generated="$project_dir/target/frontend-react/$relative_path"
  if [ -f "$generated" ] && ! diff -q "$snapshot" "$generated" > /dev/null 2>&1; then
    cp "$generated" "$snapshot"
    echo "Updated: $snapshot"
  fi
done
```

**Step 4**: Rebuild to verify all snapshots pass:

```bash
mvn clean install
```

### Checklist for Template/Helper Changes

- [ ] Made changes to `.hbs` templates or Java helper classes
- [ ] Rebuilt the main module: `mvn clean install -pl judo-ui-react`
- [ ] Built itest projects to regenerate output
- [ ] Compared and updated all mismatched snapshots
- [ ] Full build passes: `mvn clean install`
- [ ] Committed updated snapshot files alongside template/helper changes

## Common Development Tasks

### Adding a New Helper Method

1. Choose the appropriate helper class (or create new one)
2. Add public static method:

```java
@TemplateHelper
public class UiGeneralHelper extends StaticMethodValueResolver {
    
    public static String myNewHelper(Object obj) {
        // Implementation
        return result;
    }
}
```

3. Use in templates:
```handlebars
{{myNewHelper someValue}}
```

4. Use in SpringEL:
```yaml
pathExpression: "#myNewHelper(#self.name)"
```

### Modifying a Template

1. Find template in `judo-ui-react/src/main/resources/actor/`
2. Edit the `.hbs` file
3. Rebuild: `mvn clean install -pl judo-ui-react`
4. Test with integration test project
5. **Update snapshots** (see [Snapshot Testing](#snapshot-testing) below)

### Adding a New Component Template

1. Create template file in appropriate directory
2. Add entry to `ui-react.yaml`:

```yaml
- name: actor/src/components/MyComponent.tsx
  pathExpression: "'src/components/MyComponent.tsx'"
  templateName: actor/src/components/MyComponent.tsx.hbs
  conditionExpression: "#someCondition(#application)"
```

3. Create the Handlebars template file

### Debugging Template Issues

1. Check template syntax (Handlebars)
2. Verify SpringEL expressions
3. Check helper method availability
4. Enable debug logging:

```xml
<!-- logback-test.xml -->
<logger name="hu.blackbelt.judo.generator" level="DEBUG"/>
```

## MUI (Material-UI) Integration

### License Plans

The generator supports different MUI license tiers:

| Plan | Features |
|------|----------|
| Community | Basic DataGrid, standard components |
| Pro | Advanced DataGrid features, date pickers |
| Premium | All Pro features + premium components |

Configuration via helper: `ReactStoredVariableHelper.getMUILicensePlan()`

### Theme Customization

Templates in `src/theme/`:
- `palette.ts.hbs` - Color palette
- `typography.ts.hbs` - Font configuration
- `components.ts.hbs` - Component overrides

## Internationalization (i18n)

### Translation Files

Generated in `public/i18n/`:
- `application_default.json` - App-specific translations
- `system_*.json` - Framework translations

### Translation Key Generation

Helper methods in `UiI18NHelper`:
- `i18nMenuTreeLabels()` - Menu labels
- `i18nEnumerationTypes()` - Enum value labels

### Usage in Components

```typescript
import { useL10n } from '~/hooks';

const { t } = useL10n();
return <button>{t('page.create')}</button>;
```

## Troubleshooting

### Build Failures

**Symptom**: Maven build fails

**Solutions**:
1. Check Java version: `java -version` (need Java 21)
2. Clear Maven cache: `rm -rf ~/.m2/repository/hu/blackbelt/`
3. Run with debug: `mvn clean install -X`

### Template Not Found

**Symptom**: `TemplateNotFoundException`

**Solutions**:
1. Verify template path in `ui-react.yaml`
2. Check file exists in `src/main/resources/actor/`
3. Verify file extension is `.hbs`

### Helper Method Not Found

**Symptom**: `SpelEvaluationException`

**Solutions**:
1. Verify `@TemplateHelper` annotation on class
2. Check method is `public static`
3. Verify method signature (0 or 1 parameter)

### Generated Code Issues

**Symptom**: TypeScript compilation errors in generated code

**Solutions**:
1. Check template syntax
2. Verify model data is correct
3. Check import statements in template
4. Review generated code for missing dependencies

## Recent Changes

### Current Branch: `feature/JNG-6368_CollapsableFilterPanel`

Recent commits:
1. `feaffc60` - JNG-6368 Collapsable filter panel
2. `5dfb64bc` - JNG-6367 improve responsiveness
3. `48f81867` - JNG-6364 bump deps
4. `b1be9b9e` - fix errors and warnings
5. `8ad02923` - fix translation key generation

### Active Features
- Collapsible filter panels with auto-overflow
- Improved responsive design
- Updated dependencies
- Translation key generation fixes

## Quick Reference

### File Patterns

| Pattern | Purpose |
|---------|---------|
| `*.hbs` | Handlebars template |
| `*.tsx.hbs` | React TypeScript component template |
| `*.ts.hbs` | TypeScript file template |
| `*.json.hbs` | JSON configuration template |
| `ui-react.yaml` | Template descriptor |

### Key Directories

| Directory | Contents |
|-----------|----------|
| `actor/src/components/` | Reusable component templates |
| `actor/src/pages/` | Page component templates |
| `actor/src/containers/` | Container component templates |
| `actor/src/hooks/` | React hook templates |
| `actor/src/layout/` | Layout component templates |

### Helper Class Quick Reference

```java
// String transformations
UiGeneralHelper.pathName(fqName)        // "My.ClassName" → "my/class-name"
UiGeneralHelper.safeName(element)       // Safe PascalCase name
UiGeneralHelper.toUnderscore(fqName)    // "MyClass" → "my_class"

// Page utilities
UiPageHelper.getPagesForRouting(app)    // Get routable pages
UiPageHelper.getPageRoute(page)         // Get page route path
UiPageHelper.pagePath(page)             // Get page file path

// Container utilities
UiPageContainerHelper.containerPath(c)  // Get container file path
UiPageContainerHelper.containerComponentName(c)  // Get component name

// Table utilities
UiTableHelper.getFilterTypeForAttribute(attr)  // Get filter type
UiTableHelper.columnWidth(col)          // Get column width
```

---

**Last Updated**: 2026-01-27
**Version**: Based on commit `feaffc60`
**Maintainer**: BlackBelt Technology
