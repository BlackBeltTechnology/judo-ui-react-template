# Concerns

## Code Complexity

### Large Helper Classes
- `UiPageContainerHelper.java` (769 lines) - Largest helper, handles container layout, visual element collection, action definitions, and multiple concerns mixed together
- `UiWidgetHelper.java` (623 lines) - Widget rendering logic with many conditional branches
- `UiActionsHelper.java` (577 lines) - Complex action definition processing with deep EMF model traversal
- `UiPageHelper.java` (612 lines) - Page routing and navigation with extensive stream operations

### Large Templates
- `LazyTable.tsx.hbs` (866 lines) - Very complex server-side table template
- `EagerTable.tsx.hbs` (689 lines) - Client-side table template
- `filter-helper.ts.hbs` (652 lines) - Filter logic template
- Total: 22,590 lines across 321 templates

### Template Registry Size
- `ui-react.yaml` at 1,170 lines is a single monolithic configuration file
- All template mappings in one file - any error affects the entire generation

## No TODO/FIXME Comments
- Zero TODO/FIXME/HACK/XXX comments found in Java or template files
- This could indicate either very clean code or that known issues are tracked elsewhere

## Potential Fragility

### Thread-Local State
- `ReactStoredVariableHelper` uses `ThreadLocalContextHolder` for template parameters
- Synchronized methods protect access, but thread-local state is inherently fragile
- Template parameters (MUI plan, language, etc.) are strings parsed at runtime

### Tight Coupling to EMF Model
- All helpers directly traverse EMF `EObject` trees with casts
- Changes to the UI metamodel require updates across multiple helper classes
- No abstraction layer between EMF model and template helpers

### Unchecked Casts
- Several places use unchecked casts from EMF model: `((List<Link>) c.getLinks())`
- Pattern: `((ActionDefinition) a).getIsRefreshAction()` - assumes model type correctness

### `container.getChildren().size() > 0` Pattern
- `UiPageContainerHelper.java:53` uses `.size() > 0` instead of `.isEmpty()` - minor style issue
- Redundant `instanceof` check: `if (container instanceof PageContainer &&` when `container` is already typed as `PageContainer`

## Duplicate Entry in ui-react.yaml
- Lines 1136-1142: `actor/src/layout/Header/index.tsx` is registered twice with identical configuration
- This may cause the file to be generated twice (wasteful but not harmful)

## Spring Expression 5.0.0
- Using very old Spring Expression version (5.0.0.RELEASE from 2017)
- May have known vulnerabilities or missing features
- Should consider upgrading or at least auditing

## Snapshot Test Coverage
- Only specific files are snapshot-tested per itest module (not all generated files)
- Changes to non-snapshot-tested templates could regress silently
- No automated way to detect which templates need new snapshot coverage

## Build Time
- Full build generates complete React apps for each itest module
- Each itest runs `pnpm install` + `pnpm run build` which is time-intensive
- Parallel build script (`full-build-parallel.sh`) mitigates but doesn't eliminate the issue

## No Unit Tests for Java Helpers
- Helper classes have no direct unit tests
- All testing is through integration tests (generate + build + snapshot diff)
- A bug in helper logic only surfaces when the generated code fails to compile or differs from snapshots
