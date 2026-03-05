# Stack

## Languages
- **Java 21** — Generator core (helper classes, Maven plugins)
- **Handlebars** — Template language (321 `.hbs` files)
- **TypeScript/TSX** — Generated output (React frontend code)
- **YAML** — Template registry (`ui-react.yaml`, 1170 lines)
- **XML** — Maven POMs, EMF/XMI models

## Runtime & Build
- **Java 21** (source/target in `pom.xml`)
- **Maven 3.9.4** — Build system (multi-module reactor)
- **Node 22.14.0** / **pnpm 9.15.9** — For building generated React apps (via `frontend-maven-plugin 1.12.1`)
- **OSGi Bundle** — Core module packaged as OSGi bundle (`maven-bundle-plugin`)

## Frameworks & Libraries (Generator Side)
- **Eclipse EMF** (`ecore-xmi 2.2.3`) — Metamodel / model loading for JUDO UI models
- **Spring Expression Language (SpEL)** (`spring-expression 5.0.0.RELEASE`) — Expressions in template registry for path/factory/condition
- **Lombok** (`1.18.34`) — Boilerplate reduction in Java helpers
- **judo-generator-commons** — Base framework providing `@TemplateHelper`, `@ContextAccessor`, `StaticMethodValueResolver`, `ThreadLocalContextHolder`
- **judo-meta-ui** — UI metamodel types (`Application`, `PageDefinition`, `PageContainer`, `VisualElement`, etc.)
- **judo-ui-typescript-rest-commons** — Shared helper utilities (`UiCommonsHelper`)
- **JUnit Jupiter 5.5.1** — Unit testing
- **Logback 1.5.12** / **SLF4J 2.0.16** — Logging
- **JaCoCo 0.8.12** — Code coverage

## Generated App Stack
- **React 19** + **TypeScript** + **Vite**
- **MUI (Material UI) 6.x** — Component library (community or Pro/Premium via `muiLicensePlan` parameter)
- **MUI X DataGrid** — Tables (community `DataGrid` or `DataGridPro`/`DataGridPremium`)
- **Pandino** — OSGi-style dependency injection for runtime extensibility (`@pandino/pandino-api`, `@pandino/react-hooks`)
- **react-i18next** — Internationalization
- **react-router-dom** — Routing
- **Axios** — HTTP client (via generated service layer)
- **Biome** — Formatting/linting (replaces ESLint/Prettier)
- **uuid** — ID generation

## Key Dependencies (from root `pom.xml`)
| Dependency | Version | Purpose |
|---|---|---|
| `judo-meta-ui` | `1.1.0.20260223_*_develop` | UI metamodel |
| `judo-generator-commons` | `1.0.0.20260219_*_develop` | Generator framework |
| `judo-ui-typescript-rest-template` | `1.0.0.20260224_*_develop` | REST/TypeScript service generation |
| `frontend-maven-plugin` | `1.12.1` | Node/pnpm installation & execution |
| `flatten-maven-plugin` | `1.5.0` | CI-friendly `${revision}` versioning |

## Configuration
- Template parameters passed via Maven POM properties: `muiLicensePlan`, `tablePageLimit`, `defaultLanguage`, `debugPrint`, `useInlineColumnFilters`, `customComponentAnnotationPrefix`
- Accessed at runtime via `ReactStoredVariableHelper` / `ThreadLocalContextHolder`
- Template registry: `judo-ui-react/src/main/resources/ui-react.yaml`
