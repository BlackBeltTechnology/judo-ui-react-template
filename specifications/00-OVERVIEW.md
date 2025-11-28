# System Overview - JUDO UI React Runtime Model Generation

## Executive Summary

This document provides a high-level overview of the JUDO UI React Runtime Model Generation system, which transforms the code generation approach from generating thousands of lines of imperative TypeScript code to generating compact, declarative runtime models that are interpreted by smart runtime components.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    JUDO Meta-UI Metamodel                        │
│                         (ui.ecore)                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Input
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Java Generator                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Metamodel  │  │   Extractor  │  │   Template   │         │
│  │   Readers    │─▶│   Helpers    │─▶│   Engine     │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Generates
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Generated TypeScript                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Runtime Models (Declarative JSON-like structures)       │  │
│  │  - PageModel                                              │  │
│  │  - ContainerModel                                         │  │
│  │  - VisualElementModel[]                                   │  │
│  │  - ActionModel[]                                          │  │
│  │  - ValidationRuleModel[]                                  │  │
│  │  - I18nModel                                              │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Page Components (Thin wrappers)                         │  │
│  │  - Import model                                           │  │
│  │  - Create service instance                               │  │
│  │  - Render ModelDrivenPage                                │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Uses
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              Runtime Component Library (Static)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ ModelDriven  │  │VisualElement │  │   Action     │         │
│  │    Page      │─▶│  Registry    │  │  Executor    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Field      │  │  Validation  │  │    Hooks     │         │
│  │   Manager    │  │   Engine     │  │   System     │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────────────────────────┘
                             │
                             │ Renders
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      React UI Application                        │
└─────────────────────────────────────────────────────────────────┘
```

## Key Concepts

### 1. Metamodel (ui.ecore)

The source of truth - defines:
- **Visual Elements:** Inputs, containers, tables, buttons, etc.
- **Actions:** CRUD, navigation, operations, confirmations
- **Data Model:** Classes, relations, attributes, operations
- **Layout:** Flex, alignment, sizing
- **Behaviors:** Visibility, enablement, validation

### 2. Runtime Model

**Instead of generating code, generate data:**

```typescript
// Generated (180 lines)
export const UserFormPageModel: PageModel = {
  id: 'page-123',
  name: 'UserForm',
  type: 'form',
  container: {
    visualElements: [
      { type: 'textInput', name: 'firstName', col: 6, ... },
      { type: 'textInput', name: 'lastName', col: 6, ... },
    ],
    actionButtonGroups: [
      { buttons: [{ action: 'refresh' }, { action: 'update' }] }
    ]
  },
  actions: [
    { id: 'refresh', type: 'refresh', ... },
    { id: 'update', type: 'update', ... }
  ]
};
```

**Benefits:**
- Compact and readable
- Easy to debug (inspect at runtime)
- Easy to customize (override properties)
- Type-safe
- Self-documenting

### 3. Runtime Components

**Shared, reusable components that interpret models:**

```typescript
// Shared component (not generated)
export function ModelDrivenPage({ model, serviceImpl }) {
  const { data, actions } = usePageState(model, serviceImpl);
  
  return (
    <PageLayout model={model}>
      <ModelDrivenContainer
        model={model.container}
        data={data}
        actions={actions}
      />
    </PageLayout>
  );
}
```

**Benefits:**
- Single implementation, used by all pages
- Bug fixes apply everywhere
- Feature additions are global
- Well-tested and optimized

### 4. Generated Page

**Minimal wrapper (15 lines instead of 1000):**

```typescript
// Generated
import { UserFormPageModel } from '~/models/pages/user-form.model';
import { ModelDrivenPage } from '~/components/ModelDrivenPage';
import { UserServiceImpl } from '~/services/data-axios/UserServiceImpl';

export default function UserFormPage() {
  const serviceImpl = useMemo(() => new UserServiceImpl(judoAxiosProvider), []);
  return <ModelDrivenPage model={UserFormPageModel} serviceImpl={serviceImpl} />;
}
```

## Data Flow

### 1. Build Time (Generation)

```
Metamodel → Java Helpers → Extract Model Data → Handlebars Templates → TypeScript Models
```

**Example:**
```java
// Java helper
public static PageModelData extractPageModel(PageDefinition page) {
  PageModelData model = new PageModelData();
  model.setId(getXMIID(page));
  model.setName(page.getName());
  model.setType(getPageType(page));
  model.setVisualElements(extractVisualElements(page.getContainer()));
  model.setActions(extractActions(page));
  return model;
}
```

```handlebars
{{! page-model.hbs }}
export const {{ pageName page }}Model: PageModel = {
  id: '{{ model.id }}',
  name: '{{ model.name }}',
  type: '{{ model.type }}',
  visualElements: [
    {{# each model.visualElements }}
    { type: '{{ this.type }}', name: '{{ this.name }}', ... },
    {{/ each }}
  ],
  actions: [
    {{# each model.actions }}
    { id: '{{ this.id }}', type: '{{ this.type }}', ... },
    {{/ each }}
  ]
};
```

### 2. Runtime (Execution)

```
Model → ModelDrivenPage → Parse Model → Render Components → Execute Actions
```

**Example:**
```typescript
// Runtime interpretation
function ModelDrivenContainer({ model, data, actions }) {
  return (
    <Grid container>
      {model.visualElements.map(veModel => {
        const Component = VisualElementRegistry.get(veModel.type);
        const visible = !veModel.hiddenBy || !data[veModel.hiddenBy];
        
        if (!visible) return null;
        
        return (
          <Grid key={veModel.id} item md={veModel.col}>
            <Component
              model={veModel}
              value={data[veModel.attributeType]}
              disabled={veModel.enabledBy ? !data[veModel.enabledBy] : false}
              onChange={(val) => actions.storeDiff(veModel.attributeType, val)}
            />
          </Grid>
        );
      })}
    </Grid>
  );
}
```

## Domain Structure

### Core Domains

1. **Metamodel** - Understanding ui.ecore
   - Element definitions
   - Property mappings
   - Relationships

2. **Runtime Model** - TypeScript type definitions
   - PageModel
   - ContainerModel
   - VisualElementModel
   - ActionModel

3. **Visual Elements** - UI component implementations
   - Inputs (text, numeric, date, etc.)
   - Containers (flex, tabs)
   - Tables and columns
   - Links and buttons

4. **Actions** - Action execution system
   - CRUD actions
   - Navigation actions
   - Operation calls
   - Confirmations

5. **Components** - Runtime component library
   - ModelDrivenPage
   - ModelDrivenContainer
   - Field state management
   - Visual element registry

6. **Generators** - Java code generators
   - Model extractors
   - Template helpers
   - Handlebars templates

7. **Validation** - Validation system
   - Field validators
   - Dynamic validation
   - Error handling

8. **Data Model** - Data structure definitions
   - ClassType
   - RelationType
   - AttributeType
   - OperationType

9. **Integration** - System integration
   - Service integration
   - I18n
   - Routing
   - Customization hooks

## Processing Strategy

### Phase 1: Foundation (Parallel)

**Agent 1:** Metamodel specs (15 files)
- All ui.ecore element definitions
- Property documentation
- Relationship mappings

**Agent 2:** Data Model specs (8 files)
- ClassType, RelationType, AttributeType
- OperationType, EnumerationType
- Behaviors and capabilities

### Phase 2: Runtime Model (Sequential after Phase 1)

**Agent 3:** Runtime model types (10 files)
- Core type definitions
- Model interfaces
- Helper types

### Phase 3: Visual Elements (Parallel)

**Agent 4:** Input elements (10 files)
- TextInput, NumericInput, DateInput
- Checkbox, Select, etc.

**Agent 5:** Container elements (5 files)
- Flex, TabController, etc.

**Agent 6:** Table elements (5 files)
- Table, Column, Filter

**Agent 7:** Other elements (5 files)
- Link, Button, Label, etc.

### Phase 4: Actions & Validation (Parallel)

**Agent 8:** Action specs (15 files)
- All action types
- Action executor
- Confirmation system

**Agent 9:** Validation specs (5 files)
- Validation types
- Field validators
- Error handling

### Phase 5: Components (Sequential after Phases 2-4)

**Agent 10:** Component specs (12 files)
- ModelDrivenPage
- ModelDrivenContainer
- Hooks and utilities

### Phase 6: Generators (Sequential after all above)

**Agent 11:** Generator specs (10 files)
- Java helpers
- Model extractors
- Template specifications

### Phase 7: Integration & Examples (Final)

**Agent 12:** Integration specs (5 files)
- Service integration
- I18n, routing
- Customization

**Agent 13:** Example specs (4 files)
- Concrete implementation examples
- Test cases

## File Naming Convention

```
[number]-[kebab-case-name].md

Examples:
01-page-definition.md
02-text-input.md
03-refresh-action.md
```

**Number prefix:**
- Indicates processing order within domain
- Lower numbers = higher priority/more dependencies
- Gaps allowed for future additions

## Content Requirements

Each specification must include:

### 1. Header Section
```markdown
# [Element Name] Specification

**Domain:** [Domain name]
**Status:** Draft | In Progress | Complete
**Assigned:** [Agent ID or "Unassigned"]
**Dependencies:** [List of spec files this depends on]
**Blocks:** [List of spec files that depend on this]
```

### 2. Metamodel Reference
- Full Ecore class path
- All properties with types
- Parent/child relationships
- Operations (if any)

### 3. Runtime Model Mapping
- TypeScript interface definitions
- Property mappings from metamodel
- Default values
- Optional vs required fields

### 4. Implementation Details
- Generator: Java helper methods
- Component: React component interface
- Validation: Validation rules
- Examples: 2+ concrete examples

### 5. Testing Section
- Unit test requirements
- Integration test scenarios
- Edge cases to handle

## Quality Checklist

Before marking a spec as "Complete":

- [ ] All metamodel properties documented
- [ ] TypeScript types are complete and valid
- [ ] Generator implementation is detailed
- [ ] Component implementation is clear
- [ ] At least 2 examples provided
- [ ] Dependencies explicitly listed
- [ ] Testing criteria defined
- [ ] Peer reviewed by another agent/human
- [ ] Cross-references to related specs added

## Metrics & Progress

### Size Estimates

| Domain | Files | Avg Lines | Total Lines |
|--------|-------|-----------|-------------|
| Metamodel | 15 | 200 | 3,000 |
| Runtime Model | 10 | 250 | 2,500 |
| Visual Elements | 25 | 180 | 4,500 |
| Actions | 15 | 150 | 2,250 |
| Components | 12 | 200 | 2,400 |
| Generators | 10 | 300 | 3,000 |
| Validation | 5 | 150 | 750 |
| Data Model | 8 | 200 | 1,600 |
| Integration | 5 | 180 | 900 |
| Examples | 4 | 400 | 1,600 |
| **Total** | **109** | **~210** | **~22,500** |

### Timeline Estimates

**Sequential Processing:** 20-30 hours
**Parallel Processing (13 agents):** 4-6 hours
**Review & Integration:** 2-3 hours
**Total (Parallel):** 6-9 hours

## Success Criteria

The specification set is complete when:

1. ✅ 100% metamodel coverage (all ui.ecore elements)
2. ✅ All TypeScript types defined and validated
3. ✅ All generator helpers specified
4. ✅ All runtime components specified
5. ✅ All action types covered
6. ✅ Validation system fully specified
7. ✅ Integration points documented
8. ✅ Examples demonstrate all major patterns
9. ✅ Cross-references are complete
10. ✅ All specs peer-reviewed

## Expected Outcomes

After implementing these specifications:

- **Code Reduction:** 80-90% less generated code
- **Bundle Size:** 60-70% smaller
- **Build Time:** 75-85% faster
- **Maintainability:** Dramatically improved
- **Customization:** Much easier
- **Developer Experience:** Significantly better

## Next Steps

1. Review this overview
2. Start with Phase 1 (Metamodel + Data Model)
3. Proceed through phases sequentially/parallel as planned
4. Track progress in specification README
5. Conduct reviews after each phase
6. Integrate and validate with examples

## References

- **Original Proposals:** 
  - `CODE_GENERATION_OPTIMIZATION_SUGGESTIONS.md`
  - `RUNTIME_MODEL_GENERATION_PROPOSAL.md`
  - `METAMODEL_TO_RUNTIME_MAPPING.md`
- **Metamodel:** `ui.ecore`
- **Sample Model:** `judo-ui-react-itest/ActionGroupTest/model/ActionGroupTest-ui.model`

