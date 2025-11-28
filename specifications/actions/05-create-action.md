# Create Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Create implementations  

## Overview

Create action opens a form to create a new entity instance.

## Action Model

```typescript
interface CreateActionModel extends ActionModel {
  type: 'create';
  targetType: string;
  targetPageName?: string;
  isBulk: false;
  isTransient: false;
}
```

## Implementation

```typescript
async function executeCreate(action: CreateActionModel, context: ActionContext): Promise<ActionResult> {
  // Open form dialog or navigate to create page
  if (action.targetPageName) {
    const formData = await openFormDialog(action.targetPageName);
    if (formData) {
      const newEntity = await context.service.create(formData);
      return {
        success: true,
        data: newEntity,
        shouldRefresh: true,
        shouldNavigate: `/entities/${newEntity.id}`,
        message: 'Entity created successfully'
      };
    }
  }
  
  return { success: false };
}
```

## Examples

```typescript
const createAction: CreateActionModel = {
  id: 'action-create',
  name: 'create',
  type: 'create',
  targetType: 'User',
  targetPageName: 'CreateUserForm',
  isBulk: false,
  isTransient: false
};
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

