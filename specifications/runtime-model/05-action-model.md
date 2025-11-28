# ActionModel Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-core-types.md`, `metamodel/12-button-and-button-group.md`  
**Blocks:** Action execution, button rendering  

## Overview

`ActionModel` represents executable actions in the UI including CRUD operations, navigation, relation management, and custom operations. Actions are triggered by buttons, row actions, or programmatically.

## TypeScript Interface

```typescript
interface ActionModel extends BaseModel {
  // Identification
  id: string;
  name: string;
  type: ActionType;
  
  // Target
  targetType?: string;                  // ClassType for CRUD actions
  targetPageName?: string;              // Page to navigate to
  targetPagePath?: string;              // Generated route path
  
  // Operation
  operationName?: string;               // OperationType for callOperation
  operationKind?: OperationKind;        // 'static' | 'instance'
  operationParameters?: OperationParameterModel[];
  
  // Relation
  relationName?: string;                // For add/remove/set/unset
  relationFqn?: string;
  relationCardinality?: CardinalityType;
  
  // Behavior
  isBulk: boolean;                      // Operates on multiple entities
  isTransient: boolean;                 // Doesn't persist changes
  
  // Confirmation
  confirmation?: ConfirmationModel;
  
  // Pre-fetch
  preFetchActionName?: string;          // Action to run before main action
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}
```

## ConfirmationModel

```typescript
interface ConfirmationModel {
  type: 'conditional' | 'mandatory';
  title?: string;
  message: string;
  confirmLabel?: string;                // Default: 'Confirm'
  cancelLabel?: string;                 // Default: 'Cancel'
  condition?: string;                   // Expression for conditional confirmation
}
```

## OperationParameterModel

```typescript
interface OperationParameterModel {
  name: string;
  type: DataType | string;              // Primitive or ClassType name
  required: boolean;
  isCollection: boolean;
  defaultValue?: any;
  
  // Validation
  minValue?: number | string;
  maxValue?: number | string;
  minLength?: number;
  maxLength?: number;
  pattern?: string;
  
  // UI
  label?: string;
  inputType?: VisualElementType;
}
```

## Action Types

### CRUD Actions

```typescript
// Refresh - Reload current data
interface RefreshActionModel extends ActionModel {
  type: 'refresh';
}

// Create - Create new entity
interface CreateActionModel extends ActionModel {
  type: 'create';
  targetType: string;
  targetPageName?: string;              // Form page
}

// Update - Update entity
interface UpdateActionModel extends ActionModel {
  type: 'update';
  targetType: string;
}

// Delete - Delete entity
interface DeleteActionModel extends ActionModel {
  type: 'delete';
  targetType: string;
  confirmation: ConfirmationModel;      // Usually mandatory
}
```

### Relation Actions

```typescript
// Add - Add to MANY relation
interface AddActionModel extends ActionModel {
  type: 'add';
  relationName: string;
  relationFqn: string;
  targetType: string;
  relationCardinality: 'many';
}

// Remove - Remove from MANY relation
interface RemoveActionModel extends ActionModel {
  type: 'remove';
  relationName: string;
  relationFqn: string;
  relationCardinality: 'many';
}

// Set - Set ONE relation
interface SetActionModel extends ActionModel {
  type: 'set';
  relationName: string;
  relationFqn: string;
  targetType: string;
  relationCardinality: 'one';
}

// Unset - Clear ONE relation
interface UnsetActionModel extends ActionModel {
  type: 'unset';
  relationName: string;
  relationFqn: string;
  relationCardinality: 'one';
}
```

### Navigation Actions

```typescript
// OpenPage - Navigate to page
interface OpenPageActionModel extends ActionModel {
  type: 'openPage';
  targetPageName: string;
  targetPagePath: string;
}

// OpenForm - Open form dialog
interface OpenFormActionModel extends ActionModel {
  type: 'openForm';
  targetPageName: string;
  targetType?: string;
}

// OpenSelector - Open selector dialog
interface OpenSelectorActionModel extends ActionModel {
  type: 'openSelector';
  targetPageName: string;
  targetType: string;
  relationName?: string;                // For relation selectors
  allowMultiple: boolean;
}

// Back/Close
interface CloseActionModel extends ActionModel {
  type: 'close' | 'back';
}
```

### Operation Actions

```typescript
// CallOperation - Execute custom operation
interface CallOperationActionModel extends ActionModel {
  type: 'callOperation';
  operationName: string;
  operationKind: OperationKind;
  operationParameters: OperationParameterModel[];
  targetType?: string;                  // Return type
}
```

## Examples

### Example 1: Refresh Action

```typescript
const refreshAction: RefreshActionModel = {
  id: 'action-refresh',
  name: 'refresh',
  type: 'refresh',
  isBulk: false,
  isTransient: false
};
```

### Example 2: Create with Confirmation

```typescript
const createAction: CreateActionModel = {
  id: 'action-create',
  name: 'create',
  type: 'create',
  targetType: 'User',
  targetPageName: 'CreateUserForm',
  isBulk: false,
  isTransient: false,
  confirmation: {
    type: 'conditional',
    message: 'Create new user?',
    condition: 'hasUnsavedChanges'
  }
};
```

### Example 3: Delete with Mandatory Confirmation

```typescript
const deleteAction: DeleteActionModel = {
  id: 'action-delete',
  name: 'delete',
  type: 'delete',
  targetType: 'User',
  isBulk: false,
  isTransient: false,
  confirmation: {
    type: 'mandatory',
    title: 'Confirm Delete',
    message: 'Are you sure you want to delete this user? This action cannot be undone.',
    confirmLabel: 'Delete',
    cancelLabel: 'Cancel'
  }
};
```

### Example 4: Add to Relation

```typescript
const addPostAction: AddActionModel = {
  id: 'action-add-post',
  name: 'addPost',
  type: 'add',
  relationName: 'posts',
  relationFqn: 'User.posts',
  targetType: 'Post',
  relationCardinality: 'many',
  isBulk: false,
  isTransient: false
};
```

### Example 5: Call Operation with Parameters

```typescript
const changePasswordAction: CallOperationActionModel = {
  id: 'action-change-password',
  name: 'changePassword',
  type: 'callOperation',
  operationName: 'changePassword',
  operationKind: 'instance',
  targetType: 'User',
  operationParameters: [
    {
      name: 'oldPassword',
      type: 'string',
      required: true,
      label: 'Old Password',
      inputType: 'textInput',
      annotations: { inputType: 'password' }
    },
    {
      name: 'newPassword',
      type: 'string',
      required: true,
      minLength: 8,
      label: 'New Password',
      inputType: 'textInput',
      annotations: { inputType: 'password' }
    }
  ],
  isBulk: false,
  isTransient: false,
  confirmation: {
    type: 'mandatory',
    message: 'Change password?'
  }
};
```

### Example 6: Open Selector

```typescript
const selectUserAction: OpenSelectorActionModel = {
  id: 'action-select-user',
  name: 'selectUser',
  type: 'openSelector',
  targetPageName: 'UserSelector',
  targetType: 'User',
  allowMultiple: false,
  isBulk: false,
  isTransient: false
};
```

### Example 7: Bulk Delete

```typescript
const bulkDeleteAction: DeleteActionModel = {
  id: 'action-bulk-delete',
  name: 'bulkDelete',
  type: 'delete',
  targetType: 'User',
  isBulk: true,
  isTransient: false,
  confirmation: {
    type: 'mandatory',
    message: 'Delete {count} selected users?',
    confirmLabel: 'Delete All'
  }
};
```

### Example 8: Action with Pre-Fetch

```typescript
const editAction: OpenFormActionModel = {
  id: 'action-edit',
  name: 'edit',
  type: 'openForm',
  targetPageName: 'EditUserForm',
  targetType: 'User',
  preFetchActionName: 'getUserForUpdate',
  isBulk: false,
  isTransient: false
};
```

## Action Execution

```typescript
interface ActionExecutor {
  execute(action: ActionModel, context: ActionContext): Promise<any>;
}

interface ActionContext {
  entityId?: string;
  entityIds?: string[];                 // For bulk actions
  ownerData?: any;
  relations?: any;
  formData?: any;
  selectedRows?: any[];
}

// Example usage
async function executeAction(action: ActionModel, context: ActionContext) {
  // Show confirmation if needed
  if (action.confirmation) {
    const confirmed = await showConfirmation(action.confirmation, context);
    if (!confirmed) return;
  }
  
  // Execute pre-fetch if needed
  if (action.preFetchActionName) {
    const preFetchData = await executeAction(
      actions[action.preFetchActionName],
      context
    );
    context = { ...context, ...preFetchData };
  }
  
  // Execute main action
  switch (action.type) {
    case 'refresh':
      return await service.refresh();
    case 'create':
      return await service.create(context.formData);
    case 'update':
      return await service.update(context.entityId, context.formData);
    case 'delete':
      return await service.delete(context.entityId);
    // ... other action types
  }
}
```

## Validation Rules

- Action `name` must be unique within page
- `type` must be valid ActionType
- CRUD actions must have `targetType`
- Relation actions must have `relationName` and `relationFqn`
- Operation actions must have `operationName`
- Navigation actions must have `targetPageName`
- Bulk actions should have appropriate confirmation

## Related Specifications

**Metamodel:**
- `metamodel/12-button-and-button-group.md` - Actions triggered by buttons

**Runtime Model:**
- `01-core-types.md` - Base types
- `06-button-model.md` - Button-action binding

**Actions:**
- `actions/01-action-types.md` - Action type details
- `actions/02-action-executor.md` - Execution logic

**Components:**
- `components/07-action-executor-hook.md` - React hook

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

