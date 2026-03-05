# Architecture

## Code Generation Pipeline

```
EMF/XMI Model (.model)
    |
    v
judo-ui-generator-maven-plugin
    |
    +-- Phase 1: TypeScript REST generation (external template)
    |       -> target/frontend-react/src/services/
    |
    +-- Phase 2: React UI generation (this repo)
            |
            +-- ui-react.yaml (template registry)
            |       Maps templates to output files
            |       Uses SpEL expressions for paths and factories
            |
            +-- Java Helpers (@TemplateHelper classes)
            |       Callable from Handlebars via SpEL
            |
            +-- Handlebars Templates (.hbs)
                    321 templates generating full React app
                    -> target/frontend-react/
```

## Template Registry (`ui-react.yaml`, 1170 lines)

Central configuration mapping templates to generated files. Each entry has:
- `name` - Identifier
- `templateName` - Path to `.hbs` file
- `pathExpression` - SpEL expression for output file path
- `factoryExpression` - (optional) SpEL expression returning a collection; generates one file per item
- `templateContext` - (optional) Named variables passed to template
- `conditionExpression` - (optional) SpEL boolean for conditional generation
- `copy: true` - (optional) Copy file as-is without template processing

### Generation Patterns

**Singleton files** (one per app): config files, utility modules, layout components
```yaml
- name: actor/src/App.tsx
  pathExpression: "'src/App.tsx'"
  templateName: actor/src/App.tsx.hbs
```

**Factory files** (one per model element): pages, containers, dialogs
```yaml
- name: actor/src/pages/index.tsx
  factoryExpression: "#getPagesForRouting(#application)"
  pathExpression: "'src/pages/' + #pagePath(#self) + '/index.tsx'"
  templateName: actor/src/pages/index.tsx.hbs
  templateContext:
    - name: page
      expression: "#self"
```

## Java Helper Classes (14 files, 4255 total lines)

All annotated with `@TemplateHelper`, all static methods callable from templates via SpEL:

| Helper | Lines | Responsibility |
|--------|-------|----------------|
| `UiPageContainerHelper` | 769 | Container layout, paths, component names, visual element traversal |
| `UiWidgetHelper` | 623 | Widget template selection, size calculation, element collection |
| `UiPageHelper` | 612 | Page routing, dialog pages, navigation, refresh logic |
| `UiActionsHelper` | 577 | Action definitions, button groups, CRUD operations, operation calls |
| `UiTableHelper` | 428 | Table/grid generation, filter types, column config |
| `UiI18NHelper` | 415 | Translation key generation, i18n JSON structure |
| `UiGeneralHelper` | 239 | Path naming, safe names, custom component detection |
| `UiImportHelper` | 164 | MUI import mapping per widget type |
| `UiPandinoHelper` | 143 | Pandino interface keys, customization points |
| `ReactStoredVariableHelper` | 118 | Thread-local config (MUI plan, language, feature flags) |
| `UIMenuHelper` | 90 | Menu tree generation |
| `UiNPMHelper` | 44 | NPM package naming |
| `UiSecurityHelper` | 33 | OAuth client ID generation |
| `MaskEntry` | 74 | Data mask serialization for query customizers |

## Generated React App Architecture

```
target/frontend-react/
├── src/
│   ├── App.tsx, main.tsx, routes.tsx     # App shell and routing
│   ├── auth/                             # Authentication (OIDC)
│   ├── components/                       # Shared UI components
│   │   ├── dialog/                       # Dialog system (stackable)
│   │   ├── table/                        # DataGrid wrappers (Lazy/Eager)
│   │   └── widgets/                      # Reusable input widgets
│   ├── components-api/                   # TypeScript interfaces for components
│   ├── config/                           # App configuration
│   ├── containers/                       # Per-model containers (factory-generated)
│   │   └── {Container}/                  # Each has: component, dialog, page, types, customization
│   ├── custom/                           # Pandino customization hooks (.default files)
│   ├── dialogs/                          # Per-model dialogs (factory-generated)
│   ├── hooks/                            # React hooks (CRUD, config, navigation, etc.)
│   ├── l10n/                             # Localization context
│   ├── layout/                           # App shell layout (Drawer, Header, Footer)
│   ├── pages/                            # Per-model pages (factory-generated)
│   ├── services/                         # Generated REST layer (from Phase 1)
│   ├── theme/                            # MUI theme configuration
│   └── utilities/                        # Shared utilities (filtering, error handling)
└── public/
    └── i18n/                             # Translation JSON files
```

## Key Data Flow

1. **Model -> Helpers**: SpEL expressions in `ui-react.yaml` call helper methods with model objects
2. **Helpers -> Templates**: Helper methods process EMF model objects, return data structures
3. **Templates -> Generated Code**: Handlebars templates use helper results + fragment includes to produce TypeScript/TSX
4. **Fragment Composition**: Templates use `{{> fragment.hbs}}` for reusable template parts (actions, containers, table columns)
