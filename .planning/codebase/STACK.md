# Stack

## Languages & Runtimes

- **Java 21** - Core generator language (Maven compiler source/target 21)
- **Handlebars (.hbs)** - Template engine for code generation (321 templates)
- **TypeScript/React** - Target output language (generated apps)
- **YAML** - Template registry configuration (`ui-react.yaml`)

## Build System

- **Maven 3.9.4** - Primary build tool with multi-module reactor
- **frontend-maven-plugin 1.12.1** - Node.js/pnpm lifecycle integration in itests
- **maven-bundle-plugin** - OSGi bundle packaging for `judo-ui-react` module
- **flatten-maven-plugin** - CI-friendly `${revision}` version management
- **Node 22.14.0 / pnpm 9.15.9** - Auto-installed for itest frontend builds

## Key Dependencies

### Generator Core (`judo-ui-react/pom.xml`)
- `hu.blackbelt.judo.meta:hu.blackbelt.judo.meta.ui.model` - JUDO UI metamodel (EMF-based)
- `hu.blackbelt.judo.generator:judo-generator-commons` - Shared generator infrastructure (`@TemplateHelper`, `@ContextAccessor`, SpEL integration)
- `hu.blackbelt.judo.generator:judo-ui-typescript-rest-commons` - Shared TypeScript REST utilities
- `org.eclipse.emf:ecore-xmi` - EMF/XMI model loading
- `org.springframework:spring-expression 5.0.0` - SpEL for template expressions in `ui-react.yaml`
- `org.projectlombok:lombok 1.18.34` - Boilerplate reduction (`@Log`)

### Diff Checker Plugin (`judo-diff-checker-maven-plugin/pom.xml`)
- `io.github.java-diff-utils:java-diff-utils 4.12` - File diff comparison for snapshot testing

### Generated App Stack (from templates)
- React 19 + TypeScript + Vite
- MUI (Material UI) 6.x with optional DataGridPro/Premium
- Pandino - OSGi-style runtime DI for extensibility hooks
- Biome - Formatting/linting (replaces ESLint/Prettier)
- Axios - HTTP client for REST API calls
- i18n via JSON files (`application_*.json`, `system_*.json`)

## Configuration

### Template Parameters (passed via itest POMs)
- `muiLicensePlan` - community/pro/premium (affects DataGrid and Picker components)
- `tablePageLimit` - Pagination default
- `defaultLanguage` - i18n default (e.g., "en-US")
- `debugPrint` - Template debug output toggle
- `appModelName`, `appScope`, `appVersion` - Application metadata
- `useTableContextMenus`, `useTableRowHighlighting` - Feature flags
- `useInlineColumnFilters` - Requires MUI Pro license
- `customComponentAnnotationPrefix` - Pandino customization prefix (default: "use")

## Code Quality
- JaCoCo for coverage
- SonarQube integration via `sonar-maven-plugin`
- EPL-2.0 license headers on source files
