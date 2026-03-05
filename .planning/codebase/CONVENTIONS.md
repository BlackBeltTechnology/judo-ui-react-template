# Conventions

## Java Code Style

### Helper Class Pattern
All generator helpers follow a consistent pattern:
```java
@Log
@TemplateHelper
public class Ui<Domain>Helper {
    // All methods are public static
    // Callable from Handlebars templates via SpEL
    public static ReturnType methodName(ModelType param) { ... }
}
```

- `@TemplateHelper` annotation registers the class for SpEL resolution
- `@ContextAccessor` on `ReactStoredVariableHelper` enables thread-local variable access
- All helper methods are **static** (no instance state)
- `@Log` (Lombok) for logging

### EMF Model Navigation
- Heavy use of Java Streams for model traversal
- Pattern: collect elements matching conditions into lists/sets
```java
List<Something> items = new ArrayList<>();
collectElementsOfType(container, items, Something.class);
```
- `VisualElement` tree walking via recursive collection helpers in `UiWidgetHelper`

### Naming Transformations
- `pathName()` - FQ names to kebab-case paths (`::`/`.`/`#`/`/` -> `-`)
- `safeName()` - FQ names to PascalCase (`::` split + capitalize)
- `containerPath()` - Container FQ name to directory path
- `containerComponentName()` - Container FQ name to React component name
- `camelCaseNameToInterfaceKey()` - camelCase to UPPER_SNAKE_CASE for Pandino keys

## Handlebars Template Conventions

### Template Structure
- Templates mirror the generated React app directory structure
- Fragment templates use `{{> fragment.hbs}}` syntax for composition
- Widget-specific rendering via dynamic template selection: `getWidgetTemplate()` returns path based on widget type class name

### Template Types
1. **Singleton templates** - One output file per application (config, layout, utilities)
2. **Factory templates** - One output per model element (pages, containers, dialogs)
3. **Fragment templates** - Included by other templates, not directly registered in `ui-react.yaml`
4. **Copy files** - Static files copied as-is (`copy: true`)

### Largest Templates (by lines)
- `LazyTable.tsx.hbs` (866 lines) - Lazy-loaded DataGrid with server-side operations
- `EagerTable.tsx.hbs` (689 lines) - Client-side DataGrid
- `filter-helper.ts.hbs` (652 lines) - Filter logic
- `dialogs/index.tsx.hbs` (499 lines) - Dialog page rendering
- `pages/index.tsx.hbs` (434 lines) - Routed page rendering

## Configuration Conventions

### SpEL Expressions in ui-react.yaml
- `#application` - Root application model object
- `#self` - Current item in factory iteration
- `#methodName(#param)` - Calls static helper methods
- String concatenation for paths: `'src/pages/' + #pagePath(#self) + '/index.tsx'`

### Template Parameters
- Passed via Maven POM `<templateParameters>` block
- Accessed via `ReactStoredVariableHelper` thread-local
- Boolean flags as strings (parsed with `.equals("true")`)

## Error Handling
- Helper methods generally return empty collections/null rather than throwing
- Templates use conditional blocks (`{{#if}}`) to handle missing data
- No explicit error handling in most helpers - EMF model assumed valid

## Import Management
- `UiImportHelper` maintains static maps of MUI component imports per widget type
- Widget type -> Set of required MUI imports (e.g., `"textinput"` -> `{"TextField", "InputAdornment"}`)
