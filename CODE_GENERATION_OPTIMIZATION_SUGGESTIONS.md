# Code Generation Optimization Suggestions

## Executive Summary

This document outlines optimization opportunities for the JUDO React UI Generator that generates TypeScript/React code from judo-meta-ui metamodel-based models. The analysis identified significant opportunities to reduce code duplication, improve maintainability, and decrease the overall size of generated code.

**Current State:**
- 640+ Handlebars templates
- Generates large page files (500-1000+ lines)
- Heavy repetition of conditional logic and prop drilling
- Duplicate validation and action handling patterns across components

---

## 1. Button & Action Definition Consolidation

### Current Problem
The generator creates extensive duplicate code for buttons across pages and containers:

```typescript
// In page.tsx.hbs - repeated for each button
{{{ containerButtonAvailable button }}} && actions.{{ simpleActionDefinitionName actionDefinition }} && (
  <Grid className="page-action" item>
    <LoadingButton
      loading={isLoading}
      disabled={ {{{ containerButtonGroupButtonDisabledConditions button container }}} }
      onClick={ async () => {
        // action logic
      } }
    >
      {t('{{ getTranslationKeyForVisualElement button }}', { defaultValue: '{{ button.label }}' })}
    </LoadingButton>
  </Grid>
)
```

**Issues:**
- Same pattern repeated 5-15 times per page
- Complex disabled conditions recalculated inline (`!data.{{ button.enabledBy.name }} || editMode || isLoading`)
- Button availability checks duplicated
- Tooltip wrapping inconsistent

### Proposed Solution 1.1: Button Configuration Object

Extract button definitions to a configuration object:

```typescript
// Generated once per page/container
const buttonConfigs: ActionButtonConfig[] = [
  {
    id: 'action-refresh',
    actionName: 'refresh',
    label: 'judo.pages.table.refresh',
    icon: 'refresh',
    tooltip: 'judo.pages.table.refresh.tooltip',
    variant: 'text',
    disabledWhen: (data, editMode, isLoading) => isLoading || editMode,
    availableWhen: (actions) => !!actions.refresh,
    onClick: (actions, data) => actions.refresh(processQueryCustomizer(queryCustomizer)),
  },
  // ... other buttons
];
```

**Benefits:**
- Reduce 200-500 lines per large page file
- Single source of truth for button configuration
- Easier to test and maintain
- Conditional logic moved to reusable functions

### Proposed Solution 1.2: Reusable ActionButton Component

Create a runtime component that handles all button logic:

```typescript
// One-time implementation in shared components
<ActionButtonGroup
  configs={buttonConfigs}
  actions={actions}
  data={data}
  editMode={editMode}
  isLoading={isLoading}
  t={t}
/>
```

**Impact:** 
- 30-40% reduction in page file size
- Consistent behavior across all buttons
- Easier to add global button features (analytics, accessibility)

---

## 2. Input Field Repetition & Validation

### Current Problem

Each input type (TextInput, NumericInput, DateInput, etc.) generates near-identical boilerplate:

```handlebars
{{! textinput.hbs - 120+ lines }}
<TextField
  required={actions?.is{{ firstToUpper child.attributeType.name }}Required 
    ? actions.is{{ firstToUpper child.attributeType.name }}Required(data, editMode) 
    : ({{# if child.requiredBy }}data.{{ child.requiredBy.name }} ||{{/ if }} {{ boolValue child.attributeType.isRequired }})}
  disabled={actions?.is{{ firstToUpper child.attributeType.name }}Disabled 
    ? actions.is{{ firstToUpper child.attributeType.name }}Disabled(data, editMode, isLoading) 
    : ({{# if child.enabledBy }}!data.{{ child.enabledBy.name }} ||{{/ if }} isLoading)}
  error={ !!validation.get('{{ child.attributeType.name }}') }
  helperText={ validation.get('{{ child.attributeType.name }}') }
  onChange={ (event) => {
    const realValue = event.target.value?.length === 0 ? null : event.target.value;
    storeDiff('{{ child.attributeType.name }}', realValue);
  } }
  // ... 20+ more props
/>
```

**Issues:**
- Same required/disabled/readonly logic repeated across ALL inputs
- Validation access pattern duplicated everywhere
- onChange handling nearly identical
- Conditional logic difficult to read and maintain
- 8-12 widget fragment templates with 80% identical code

### Proposed Solution 2.1: Common Field Configuration Hook

Generate field configurations rather than inline props:

```typescript
// Generated once per form
const fieldConfigs = useFieldConfigs(data, actions, editMode, validation, storeDiff, isLoading);

// Usage
<FormField
  config={fieldConfigs.firstName}
  component={TextField}
/>
```

**Hook implementation (shared, not generated):**
```typescript
function useFieldConfigs(data, actions, editMode, validation, storeDiff, isLoading) {
  return useMemo(() => ({
    firstName: {
      name: 'firstName',
      label: 'First Name',
      value: data.firstName ?? '',
      required: actions?.isFirstNameRequired?.(data, editMode) ?? false,
      disabled: actions?.isFirstNameDisabled?.(data, editMode, isLoading) ?? isLoading,
      readonly: actions?.isFirstNameReadonly?.(data, editMode, isLoading) ?? !isFormUpdateable(),
      error: validation.get('firstName'),
      onChange: (value) => storeDiff('firstName', value || null),
      onBlur: actions?.onFirstNameBlurAction,
    },
    // ... other fields
  }), [data, actions, editMode, validation, storeDiff, isLoading]);
}
```

### Proposed Solution 2.2: Unified Input Component

Create a smart wrapper that handles common patterns:

```typescript
// Runtime component (not generated)
export function SmartInput({ config, type = 'text', icon, maxLength, ...props }) {
  return (
    <TextField
      {...config}  // spreads name, label, value, required, disabled, error, helperText
      type={type}
      onChange={(e) => config.onChange(e.target.value)}
      onBlur={config.onBlur}
      InputProps={{
        readOnly: config.readonly,
        startAdornment: icon && <InputAdornment><MdiIcon path={icon} /></InputAdornment>,
      }}
      inputProps={{ maxLength }}
      {...props}
    />
  );
}
```

**Impact:**
- 50-60% reduction in widget fragment template size
- Eliminate ~1000 lines of repetitive code per medium application
- Centralized validation handling
- Easier to add features like inline validation feedback

---

## 3. Container Import Consolidation

### Current Problem

Each container generates 20-50 import statements with many duplicates:

```typescript
// common-imports.fragment.hbs - repeated patterns
import { useTranslation } from 'react-i18next';
import { useL10N } from '~/l10n/l10n-context';
import { useJudoNavigation } from '~/components';
import { useConfirmDialog } from '~/components/dialog';
// ... 30+ more imports
```

**Issues:**
- Every container imports the same 15-20 hooks
- MUI component imports can be consolidated
- Type imports scattered throughout
- Large import blocks (40-60 lines) in every file

### Proposed Solution 3.1: Container Prelude Module

Create import barrels for common patterns:

```typescript
// ~/containers/prelude.ts (generated once or static)
export {
  useTranslation,
  useL10N,
  useJudoNavigation,
  useConfirmDialog,
  useErrorHandler,
  // ... all common hooks
} from './common-hooks';

export {
  Grid,
  Box,
  TextField,
  Button,
  LoadingButton,
  // ... all common MUI
} from './common-components';

export type {
  Dispatch,
  SetStateAction,
  FC,
  // ... all common React types
} from 'react';
```

**Container imports become:**
```typescript
import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import * as Prelude from '~/containers/prelude';
import type { /* specific types */ } from '~/services/data-api/model';
```

**Impact:**
- 15-25 lines saved per container
- Faster IDE autocomplete
- Easier to update common dependencies
- Better tree-shaking opportunities

---

## 4. Type Definition Bloat

### Current Problem

Types are regenerated extensively even when patterns are identical:

```typescript
// types.ts.hbs - repeated for every container
export interface ContainerActionDefinitions {
  getPageTitle?: (data: UserStored) => string;
  refresh?: (queryCustomizer: UserQueryCustomizer) => Promise<JudoRestResponse<UserStored>>;
  update?: () => Promise<void>;
  // ... 15-30 action definitions
  isFirstNameRequired?: (data: User, editMode: boolean) => boolean;
  isFirstNameDisabled?: (data: User, editMode: boolean, isLoading: boolean) => boolean;
  isFirstNameReadonly?: (data: User, editMode: boolean, isLoading: boolean) => boolean;
  // ... repeated for 20+ fields
}
```

**Issues:**
- Field modifier patterns identical across all containers
- 100-300 lines of type definitions per container
- Hard to see actual custom actions among boilerplate

### Proposed Solution 4.1: Generic Field Modifier Types

Create reusable generic types:

```typescript
// types/containers.ts (shared)
export type FieldName<T> = keyof T;

export interface FieldModifiers<TData, TFields extends keyof TData = keyof TData> {
  required?: Partial<Record<TFields, (data: TData, editMode: boolean) => boolean>>;
  disabled?: Partial<Record<TFields, (data: TData, editMode: boolean, isLoading: boolean) => boolean>>;
  readonly?: Partial<Record<TFields, (data: TData, editMode: boolean, isLoading: boolean) => boolean>>;
  hidden?: Partial<Record<TFields, (data: TData, editMode: boolean) => boolean>>;
}

// Generated type becomes:
export interface UserFormActionDefinitions extends FieldModifiers<User> {
  getPageTitle?: (data: UserStored) => string;
  refresh?: (queryCustomizer: UserQueryCustomizer) => Promise<JudoRestResponse<UserStored>>;
  // ... only custom actions
}
```

**Impact:**
- 40-50% reduction in type definition size
- Better type inference
- Clearer separation of concerns
- Easier to document and understand

---

## 5. Action Implementation Patterns

### Current Problem

Action implementations in pages follow very repetitive patterns:

```typescript
// index.tsx.hbs - repeated 10-20 times
const refreshAction = useCallback(async () => {
  setIsLoading(true);
  try {
    const response = await userServiceImpl.refresh(processQueryCustomizer(queryCustomizer));
    setData(response.data);
    setValidation(new Map());
  } catch (error) {
    handleError(error);
  } finally {
    setIsLoading(false);
  }
}, [userServiceImpl, queryCustomizer, handleError]);
```

**Issues:**
- Loading state management duplicated
- Error handling identical
- Try-catch-finally pattern repeated
- Data setting logic same across all CRUD operations

### Proposed Solution 5.1: Action Factory Hook

Create a factory for common action patterns:

```typescript
// hooks/usePageActions.ts (shared utility)
export function usePageActions<T>(service, handleError) {
  const [isLoading, setIsLoading] = useState(false);
  
  const createAction = useCallback((handler: () => Promise<T>) => {
    return async () => {
      setIsLoading(true);
      try {
        return await handler();
      } catch (error) {
        handleError(error);
        throw error;
      } finally {
        setIsLoading(false);
      }
    };
  }, [handleError]);

  return { isLoading, createAction };
}

// Generated usage:
const { isLoading, createAction } = usePageActions(userServiceImpl, handleError);

const actions = useMemo(() => ({
  refresh: createAction(() => 
    userServiceImpl.refresh(queryCustomizer).then(res => setData(res.data))
  ),
  update: createAction(() => 
    userServiceImpl.update(data).then(() => navigateBack())
  ),
  // ... concise action definitions
}), [createAction, data, queryCustomizer]);
```

**Impact:**
- 30-40% reduction in page implementation size
- Consistent error handling
- Single loading state management
- Easier to add cross-cutting concerns (analytics, logging)

---

## 6. Conditional Rendering Patterns

### Current Problem

Conditional rendering logic is extremely verbose:

```typescript
{{{ containerButtonAvailable button }}} && 
  actions.{{ simpleActionDefinitionName actionDefinition }} && 
  actionsRequested.has('{{ actionDefinition.name }}') && (
  // render button
)}

{ (actions?.is{{ safeName child }}Hidden 
  ? !actions?.is{{ safeName child }}Hidden(data, editMode) 
  : !data.{{ child.hiddenBy.name }}) && (
  // render field
)}
```

**Issues:**
- Ternary within ternary patterns
- Difficult to read and debug
- Same patterns repeated throughout templates
- Generates 100+ lines of conditional checks per file

### Proposed Solution 6.1: Visibility Manager

Create a declarative visibility system:

```typescript
// Generated configuration
const visibilityRules = {
  refreshButton: (ctx) => ctx.actions.refresh && !ctx.editMode,
  firstNameField: (ctx) => ctx.actions?.isFirstNameHidden?.(ctx.data, ctx.editMode) ?? !ctx.data.firstNameHiddenBy,
  // ... other rules
};

const visibility = useVisibility(visibilityRules, { actions, data, editMode });

// Usage becomes:
{visibility.refreshButton && <RefreshButton />}
{visibility.firstNameField && <FirstNameInput />}
```

**Impact:**
- 20-30% reduction in component render logic
- Easier to debug visibility issues
- Testable visibility rules
- Better performance (memoized checks)

---

## 7. Template Fragmentation

### Current Problem

Templates are split into many small fragments that still generate repetitive code:

```
actor/src/containers/widget-fragments/
├── textinput.hbs (120 lines)
├── numericinput.hbs (110 lines)
├── dateinput.hbs (95 lines)
├── textarea.hbs (115 lines)
└── ... 20+ more similar files
```

**Issues:**
- 80% code similarity across input widgets
- Same conditional patterns repeated
- Maintenance nightmare (change needs to be applied to 10+ files)
- Template inheritance limited

### Proposed Solution 7.1: Template Composition with Shared Partials

Create truly reusable partial templates:

```handlebars
{{! shared-input-wrapper.hbs }}
{{# if child.hiddenBy }} 
  { {{> visibility-check }} && 
{{/ if }}
<Grid item xs={12} sm={12} md={{ calculateSize child }}>
  {{> custom-component-wrapper }}
  {{> sub-theme-wrapper }}
  {{> tooltip-wrapper }}
  
  {{> @partial-block }}  {{! Content goes here }}
  
  {{> /tooltip-wrapper }}
  {{> /sub-theme-wrapper }}
  {{> /custom-component-wrapper }}
</Grid>
{{# if child.hiddenBy }} } {{/ if }}
```

```handlebars
{{! textinput.hbs - reduced from 120 to 30 lines }}
{{#> shared-input-wrapper child=child }}
  <TextField
    {{> common-input-props }}
    type="text"
    {{# if child.attributeType.dataType.maxLength }}
    inputProps={{ maxLength: {{ child.attributeType.dataType.maxLength }} }}
    {{/ if }}
  />
{{/shared-input-wrapper}}
```

**Impact:**
- 60-70% reduction in widget template size
- Single place to fix common bugs
- Easier to add new widget types
- Better consistency

---

## 8. Props Drilling Reduction

### Current Problem

Every component receives 10-15 props that are passed through multiple levels:

```typescript
// Page → PageContainer → Container → Component
<Container
  actions={actions}
  data={data}
  editMode={editMode}
  validation={validation}
  setValidation={setValidation}
  storeDiff={storeDiff}
  isLoading={isLoading}
  isFormUpdateable={isFormUpdateable}
  isFormDeleteable={isFormDeleteable}
  refreshCounter={refreshCounter}
  // ... more props
/>
```

**Issues:**
- TypeScript interfaces become massive
- Props passed but unused in intermediate components
- Hard to refactor
- Performance implications (unnecessary re-renders)

### Proposed Solution 8.1: Context-Based State Management

Use React Context for shared state:

```typescript
// Generated context provider
const UserFormContext = createContext<{
  data: UserStored;
  actions: UserFormActions;
  editMode: boolean;
  validation: ValidationMap;
  isLoading: boolean;
  // ... other state
}>(null!);

// Page level
<UserFormContext.Provider value={{ data, actions, editMode, validation, isLoading }}>
  <UserFormContainer />
</UserFormContext.Provider>

// Deep in component tree
function FirstNameInput() {
  const { data, actions, validation, editMode } = useContext(UserFormContext);
  // No prop drilling!
}
```

**Impact:**
- 40-50% reduction in prop definitions
- Cleaner component APIs
- Better performance with selective context usage
- Easier to add new global state

---

## 9. Static vs. Dynamic Generation

### Current Recommendation

Not all code needs to be generated. Consider moving these to runtime libraries:

**Move to Static Runtime Library:**
- Input field wrappers
- Button handlers
- Validation utilities
- Common hooks
- Layout components
- Error handling
- Loading states

**Keep Generated:**
- Type definitions from metamodel
- Field configurations
- Action bindings
- Route definitions
- Service implementations
- Translations
- Form schemas

**Impact:**
- 30-40% less generated code overall
- Faster generation times
- Easier to update common logic
- Better runtime performance (shared code)
- Smaller bundle sizes

---

## 10. Implementation Priority

### Phase 1 (High Impact, Low Risk) - 2-3 weeks
1. **Button Configuration Objects** (Solution 1.1)
2. **Common Import Consolidation** (Solution 3.1)
3. **Action Factory Hook** (Solution 5.1)

**Expected Reduction:** 20-25% in generated code size

### Phase 2 (High Impact, Medium Risk) - 3-4 weeks
4. **Field Configuration Hook** (Solution 2.1)
5. **Template Fragmentation Fix** (Solution 7.1)
6. **Generic Type Definitions** (Solution 4.1)

**Expected Reduction:** Additional 20-25% in generated code size

### Phase 3 (Medium Impact, Higher Risk) - 4-6 weeks
7. **Context-Based State** (Solution 8.1)
8. **Visibility Manager** (Solution 6.1)
9. **Static Runtime Library** (Solution 9)

**Expected Reduction:** Additional 15-20% in generated code size

### Total Expected Impact

- **Overall generated code reduction:** 50-65%
- **Maintenance effort reduction:** 60-70%
- **Build time improvement:** 30-40%
- **Generated bundle size reduction:** 20-30%
- **Developer experience:** Significantly improved

---

## 11. Metrics & Success Criteria

### Before Optimization (Baseline)
- Average page file: 800-1200 lines
- Average container file: 600-900 lines
- Widget templates: 15-20 files × 100-120 lines
- Type definitions per container: 200-300 lines
- Total templates: 640+

### After Optimization (Target)
- Average page file: 400-600 lines (40-50% reduction)
- Average container file: 300-500 lines (40-45% reduction)
- Widget templates: 5-8 shared templates × 30-50 lines
- Type definitions per container: 100-150 lines (50% reduction)
- Total templates: 300-400 (35-40% reduction)

### Quality Metrics
- Code duplication index: <10% (currently >40%)
- Cyclomatic complexity: <15 per function
- Template reusability: >60% shared code
- Build time: <50% of current
- Bundle size: 20-30% smaller

---

## 12. Migration Strategy

### Backward Compatibility
1. Generate new structure alongside old (feature flag)
2. Create adapter layer for existing customizations
3. Incremental migration path for existing applications
4. Comprehensive migration guide

### Testing Strategy
1. Generate and compare outputs before/after
2. Integration tests for all common patterns
3. Performance benchmarks
4. Visual regression testing

### Documentation
1. Update template development guide
2. Create runtime library documentation
3. Migration examples
4. Best practices guide

---

## Conclusion

The current generator produces robust, working code but suffers from significant repetition and verbosity. The proposed optimizations will:

1. **Reduce generated code by 50-65%**
2. **Improve maintainability dramatically**
3. **Enable faster development iterations**
4. **Provide better developer experience**
5. **Create more consistent output**

All optimizations can be implemented incrementally with minimal risk to existing functionality. The phased approach ensures continuous validation and allows for course correction.

**Recommended Next Steps:**
1. Review and prioritize optimization suggestions
2. Create POC for Phase 1 optimizations
3. Measure actual impact on sample project
4. Adjust approach based on findings
5. Plan full implementation

---

## Appendix: Example Before/After

### Page Component - Before (Simplified)
```typescript
// ~1000 lines
export default function UserEditPage() {
  // 50 lines of state
  const [isLoading, setIsLoading] = useState(false);
  const [data, setData] = useState<UserStored>({} as UserStored);
  // ... 10+ more state variables
  
  // 200 lines of action handlers
  const refreshAction = useCallback(async () => {
    setIsLoading(true);
    try {
      const response = await userServiceImpl.refresh(queryCustomizer);
      setData(response.data);
    } catch (error) {
      handleError(error);
    } finally {
      setIsLoading(false);
    }
  }, []);
  
  // ... 15+ more similar actions
  
  // 100 lines of field modifiers
  const isFirstNameRequired = useCallback(() => {
    return data.firstNameRequired || false;
  }, [data]);
  // ... 20+ more similar functions
  
  // 300 lines of render with buttons and fields
  return (
    <PageHeader>
      {actions.refresh && actionsRequested.has('refresh') && (
        <LoadingButton loading={isLoading} onClick={refreshAction}>
          Refresh
        </LoadingButton>
      )}
      {/* ... 10+ more buttons */}
    </PageHeader>
    <Container>
      <TextField
        required={isFirstNameRequired()}
        disabled={isLoading}
        error={!!validation.get('firstName')}
        helperText={validation.get('firstName')}
        value={data.firstName ?? ''}
        onChange={(e) => storeDiff('firstName', e.target.value)}
      />
      {/* ... 20+ more fields */}
    </Container>
  );
}
```

### Page Component - After (Simplified)
```typescript
// ~400 lines
export default function UserEditPage() {
  const { data, actions, isLoading } = useUserForm();
  const fields = useFieldConfigs(data, actions);
  const visibility = useVisibility(visibilityRules, { data, actions });
  
  const actionConfigs = useMemo(() => [
    { name: 'refresh', handler: actions.refresh, icon: 'refresh' },
    { name: 'update', handler: actions.update, icon: 'save' },
    // ... concise config
  ], [actions]);
  
  return (
    <UserFormContext.Provider value={{ data, actions, fields, visibility }}>
      <PageHeader>
        <ActionButtonGroup configs={actionConfigs} />
      </PageHeader>
      <Container>
        <FormField config={fields.firstName} />
        <FormField config={fields.lastName} />
        {/* ... clean field declarations */}
      </Container>
    </UserFormContext.Provider>
  );
}
```

**Lines of code:** 1000 → 400 (60% reduction)
**Complexity:** High → Medium-Low
**Maintainability:** Difficult → Easy
**Customization:** Complex → Straightforward

