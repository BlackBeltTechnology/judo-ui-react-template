# Action Types Overview

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** All specific action implementations  

## Overview

This specification provides an overview of all action types in the JUDO system, their categorization, execution patterns, and common behaviors.

## Action Categories

### 1. CRUD Actions (4 types)

Basic data operations:

| Action | Purpose | Target | Confirmation |
|--------|---------|--------|--------------|
| **Refresh** | Reload current data | Current page | No |
| **Create** | Create new entity | Entity type | Optional |
| **Update** | Update existing entity | Single entity | Optional |
| **Delete** | Delete entity | Single/Multiple | Mandatory |

### 2. Relation Actions (4 types)

Manage relationships between entities:

| Action | Purpose | Cardinality | Confirmation |
|--------|---------|-------------|--------------|
| **Add** | Add to MANY relation | MANY | Optional |
| **Remove** | Remove from MANY relation | MANY | Optional |
| **Set** | Set ONE relation | ONE | No |
| **Unset** | Clear ONE relation | ONE | Optional |

### 3. Navigation Actions (4 types)

Navigate between pages:

| Action | Purpose | Opens | Modal |
|--------|---------|-------|-------|
| **OpenPage** | Navigate to page | Full page | No |
| **OpenForm** | Open form dialog | Dialog | Yes |
| **OpenSelector** | Select entities | Dialog | Yes |
| **Back/Close** | Return/Close | Previous | N/A |

### 4. Operation Actions (1 type)

Execute custom operations:

| Action | Purpose | Parameters | Return |
|--------|---------|------------|--------|
| **CallOperation** | Execute custom logic | Optional | Optional |

## Action Execution Flow

```typescript
async function executeAction(
  action: ActionModel,
  context: ActionContext
): Promise<any> {
  // 1. Pre-execution validation
  if (!canExecuteAction(action, context)) {
    return;
  }
  
  // 2. Show confirmation if needed
  if (action.confirmation) {
    const confirmed = await showConfirmation(action.confirmation, context);
    if (!confirmed) return;
  }
  
  // 3. Execute pre-fetch action if specified
  let preFetchData;
  if (action.preFetchActionName) {
    preFetchData = await executeAction(
      findAction(action.preFetchActionName),
      context
    );
  }
  
  // 4. Execute main action
  const result = await performAction(action, {
    ...context,
    preFetchData
  });
  
  // 5. Post-execution handling
  await handleActionResult(result, action);
  
  return result;
}
```

## Common Action Properties

All actions share these properties:

```typescript
interface BaseActionProperties {
  id: string;
  name: string;
  type: ActionType;
  isBulk: boolean;
  isTransient: boolean;
  confirmation?: ConfirmationModel;
  preFetchActionName?: string;
}
```

## Action Context

Actions execute with context:

```typescript
interface ActionContext {
  // Entity context
  entityId?: string;
  entityIds?: string[];        // For bulk actions
  entityData?: any;
  
  // Relation context
  ownerData?: any;
  relationName?: string;
  
  // Form context
  formData?: any;
  isDraft?: boolean;
  
  // Table context
  selectedRows?: any[];
  queryCustomizer?: QueryCustomizer;
  
  // Navigation context
  currentPage?: PageModel;
  navigate?: (path: string) => void;
  
  // Services
  service?: any;
  i18n?: TranslationFunction;
}
```

## Action States

Actions can be in different states:

```typescript
type ActionState = 
  | 'idle'        // Not executing
  | 'pending'     // Waiting for user input
  | 'executing'   // Currently running
  | 'success'     // Completed successfully
  | 'error';      // Failed with error

interface ActionExecution {
  actionName: string;
  state: ActionState;
  startTime?: number;
  endTime?: number;
  error?: Error;
  result?: any;
}
```

## Action Results

```typescript
interface ActionResult {
  success: boolean;
  data?: any;
  error?: string;
  shouldRefresh?: boolean;     // Whether to refresh page
  shouldNavigate?: string;     // Navigate to this path
  shouldClose?: boolean;       // Close dialog/form
  message?: string;            // Success/error message
}
```

## Conditional Execution

Actions can be conditionally enabled:

```typescript
function canExecuteAction(
  action: ActionModel,
  context: ActionContext
): boolean {
  // Check behavior conditions
  if (action.enabledBy && !context.entityData?.[action.enabledBy]) {
    return false;
  }
  
  // Check data requirements
  if (action.type === 'update' && !context.entityId) {
    return false;
  }
  
  // Check permissions
  if (!hasPermission(action, context.user)) {
    return false;
  }
  
  return true;
}
```

## Error Handling

```typescript
async function handleActionError(error: Error, action: ActionModel) {
  console.error(`Action ${action.name} failed:`, error);
  
  // Show user-friendly error
  showToast({
    severity: 'error',
    message: t(`errors.actions.${action.type}`, {
      defaultValue: `Failed to execute ${action.name}`
    })
  });
  
  // Log for debugging
  logError({
    action: action.name,
    type: action.type,
    error: error.message,
    stack: error.stack
  });
}
```

## Action Chaining

Actions can be chained:

```typescript
// Sequential execution
await executeAction(preFetchAction, context);
await executeAction(mainAction, context);
await executeAction(postAction, context);

// Conditional chaining
const result = await executeAction(checkAction, context);
if (result.success) {
  await executeAction(successAction, context);
} else {
  await executeAction(failureAction, context);
}
```

## Bulk Actions

```typescript
async function executeBulkAction(
  action: ActionModel,
  entities: any[]
): Promise<ActionResult[]> {
  const results: ActionResult[] = [];
  
  for (const entity of entities) {
    try {
      const result = await executeAction(action, {
        entityId: entity.id,
        entityData: entity
      });
      results.push({ success: true, data: result });
    } catch (error) {
      results.push({ success: false, error: error.message });
    }
  }
  
  return results;
}
```

## Action Patterns by Type

### Pattern 1: Simple CRUD

```typescript
// Refresh
await service.refresh();

// Create
const newEntity = await service.create(formData);
navigate(`/entities/${newEntity.id}`);

// Update
await service.update(entityId, formData);

// Delete
await service.delete(entityId);
navigate('/entities');
```

### Pattern 2: Relation Management

```typescript
// Add to MANY
await service.addRelation(ownerId, 'posts', relatedId);

// Remove from MANY
await service.removeRelation(ownerId, 'posts', relatedId);

// Set ONE
await service.setRelation(ownerId, 'department', relatedId);

// Unset ONE
await service.unsetRelation(ownerId, 'department');
```

### Pattern 3: Navigation

```typescript
// Open page
navigate(`/pages/UserView/${entityId}`);

// Open form
openDialog({
  component: <UserForm entityId={entityId} onClose={handleClose} />
});

// Open selector
const selected = await openSelector({
  entityType: 'User',
  multiple: false
});
```

### Pattern 4: Operation Call

```typescript
// Without parameters
await service.activate(entityId);

// With parameters
await service.changePassword(entityId, {
  oldPassword: 'old',
  newPassword: 'new'
});
```

## Testing Criteria

### Unit Tests
- [x] Action type categorization
- [x] Context validation
- [x] State transitions
- [x] Error handling
- [x] Result processing

### Integration Tests
- [x] Action execution flow
- [x] Confirmation dialogs
- [x] Pre-fetch actions
- [x] Action chaining
- [x] Bulk execution

### Edge Cases
- [x] Missing context data
- [x] Network failures
- [x] Concurrent executions
- [x] Cancel during execution
- [x] Circular dependencies

## Related Specifications

**Runtime Model:**
- `runtime-model/05-action-model.md` - Action models

**Actions:**
- `02-action-executor.md` - Execution engine
- `03-confirmation-system.md` - Confirmation handling
- `04-15-*.md` - Specific action types

**Components:**
- `components/07-action-executor-hook.md` - React hook

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

