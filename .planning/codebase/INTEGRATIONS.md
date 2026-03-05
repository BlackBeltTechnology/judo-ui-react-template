# Integrations

## Input Model

- **JUDO UI Model (EMF/XMI)** - `.model` files define the entire UI structure
  - Located at `judo-ui-react-itest/<TestName>/model/<TestName>-ui.model`
  - Metamodel: `hu.blackbelt.judo.meta.ui.model` (classes: `Application`, `PageDefinition`, `PageContainer`, `ActionDefinition`, `VisualElement`, `Table`, `Link`, `Flex`, `Button`, `ButtonGroup`, etc.)
  - EMF EObject tree traversal used extensively in helpers

## Generator Ecosystem

### judo-ui-generator-maven-plugin
- Orchestrates code generation in two phases:
  1. **Phase 1**: TypeScript REST layer via `judo-ui-typescript-rest-template` (API types, service interfaces, Axios implementations) -> `target/frontend-react/src/services/`
  2. **Phase 2**: React UI via this repo's templates -> `target/frontend-react/`

### judo-generator-commons
- Provides `@TemplateHelper` annotation for registering helper classes
- Provides `@ContextAccessor` for thread-local template variables
- `StaticMethodValueResolver` base class for Handlebars value resolution
- `ThreadLocalContextHolder` for passing template parameters to helpers

### judo-ui-typescript-rest-commons
- Shared utilities: `UiCommonsHelper` with `firstToUpper`, `firstToLower`, `classDataName`, `restParamName`, `getXMIID`

## Generated App Integrations

### Authentication
- OIDC/OAuth2 via configurable auth provider (templates in `actor/src/auth/`)
- Axios interceptor for token injection (`axiosInterceptor.ts.hbs`)
- Principal context for user info (`principal-context.tsx.hbs`)

### REST API
- Generated Axios-based service implementations (from Phase 1)
- Query customizer processing for server-side filtering/sorting
- Error handling with operation fault dialogs

### Pandino (Runtime DI)
- Interface key constants generated per visual element (e.g., `ROUTE_GOD_GALAXIES_TABLE_INTERFACE_KEY`)
- Customization hooks: pages, containers, dialogs can be overridden at runtime
- `.default` suffix files generated as starting points for customization

## Distribution
- Nexus repositories: `nexus.judo.technology` (judong), Maven Central (ossrh)
- OSGi bundles with `Web-ContextPath` for deployment
