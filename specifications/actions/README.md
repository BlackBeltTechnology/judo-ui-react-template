# Actions Domain Specifications

**Purpose:** Specify all action types and the action execution system.

**Status:** 0/15 Complete

**Processing:** Parallel possible after grouping

## Files

### Core System (Process First)
1. `01-action-types.md` - All ActionDefinition types enumeration
2. `02-action-executor.md` - Action execution engine
3. `03-confirmation-system.md` - Confirmation dialogs

### CRUD Actions (Parallel Group A)
4. `04-refresh-action.md` - RefreshActionDefinition
5. `05-create-action.md` - CreateActionDefinition
6. `06-update-action.md` - UpdateActionDefinition
7. `07-delete-action.md` - DeleteActionDefinition

### Relation Actions (Parallel Group B)
8. `08-add-action.md` - AddActionDefinition
9. `09-remove-action.md` - RemoveActionDefinition
10. `10-set-action.md` - SetActionDefinition
11. `11-unset-action.md` - UnsetActionDefinition

### Navigation Actions (Parallel Group C)
12. `12-open-page-action.md` - OpenPageActionDefinition
13. `13-open-form-action.md` - OpenFormActionDefinition
14. `14-open-selector-action.md` - OpenSelectorActionDefinition

### Operation Actions (Parallel Group D)
15. `15-call-operation-action.md` - CallOperationActionDefinition

## Dependencies

**External:**
- `metamodel/*` - Action metamodel elements
- `runtime-model/05-action-model.md` - ActionModel types

**Internal:**
- Files 01-03 must complete first (core system)
- Files 04-15 can be parallel in groups

## Assignment Recommendations

### Parallel (5 agents)
- **Agent 8A:** Files 01-03 (core)
- **Agent 8B:** Files 04-07 (CRUD) - waits for 8A
- **Agent 8C:** Files 08-11 (Relations) - waits for 8A
- **Agent 8D:** Files 12-14 (Navigation) - waits for 8A
- **Agent 8E:** File 15 (Operations) - waits for 8A

## Key Concepts

### Action Lifecycle

```
Define → Bind to Button → User Click → Confirmation? → Execute → Handle Result
```

### Action Categories

1. **Data Actions:** refresh, create, update, delete
2. **Relation Actions:** add, remove, set, unset
3. **Navigation Actions:** openPage, back, cancel
4. **Operation Actions:** callOperation (parameterless, with input form, with selector)
5. **UI Actions:** filter, clear, export

### Execution Context

```typescript
interface ActionContext {
  data: any;              // Current data
  serviceImpl: any;       // Service implementation
  selectedRows?: any[];   // For table actions
  params?: any;           // Additional parameters
}
```

## Completion Criteria

Each file must include:
- [ ] Metamodel ActionDefinition documentation
- [ ] Runtime ActionModel interface
- [ ] Execution logic specification
- [ ] Parameter requirements
- [ ] Return value specification
- [ ] Error handling
- [ ] Confirmation integration
- [ ] Service method mapping
- [ ] Examples (success and error cases)

## Output Format

```markdown
# [Action Name] Specification

**Domain:** Actions
**Metamodel:** `ui::[ActionClassName]`
**Status:** [Draft|Complete]

## Overview
[What this action does]

## Metamodel Properties
[Properties from ActionDefinition subclass]

## Runtime Model
\`\`\`typescript
interface [Action]Model extends ActionModel {
  type: '[actionType]';
  // action-specific properties
}
\`\`\`

## Execution Specification

### Prerequisites
[What must be true before execution]

### Parameters
[What parameters are needed]

### Service Method
\`\`\`typescript
serviceImpl.[methodName](params): Promise<Result>
\`\`\`

### Success Handling
[What happens on success]

### Error Handling
[How errors are handled]

### Confirmation
[If/when confirmation is needed]

## Examples

### Example 1: Success Case
[Code example]

### Example 2: Error Case
[Code example]

## Testing
[Test scenarios]
```

## Processing Time

- **Estimated time per file:** 30-40 minutes
- **Sequential:** 7.5-10 hours
- **Parallel (5 agents):** 2-2.5 hours

