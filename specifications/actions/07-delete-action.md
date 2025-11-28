# Delete Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Delete implementations  

## Overview

Delete action removes entity from system with mandatory confirmation.

## Action Model

```typescript
interface DeleteActionModel extends ActionModel {
  type: 'delete';
  targetType: string;
  isBulk: boolean;
  confirmation: ConfirmationModel;
}
```

## Implementation

```typescript
async function executeDelete(action: DeleteActionModel, context: ActionContext): Promise<ActionResult> {
  // Show confirmation
  const confirmed = await showConfirmation({
    type: 'mandatory',
    title: 'Confirm Delete',
    message: action.isBulk 
      ? `Delete ${context.entityIds?.length} entities?`
      : 'Are you sure you want to delete this entity?',
    confirmLabel: 'Delete',
    cancelLabel: 'Cancel'
  });
  
  if (!confirmed) return { success: false };
  
  try {
    if (action.isBulk && context.entityIds) {
      await Promise.all(context.entityIds.map(id => context.service.delete(id)));
    } else if (context.entityId) {
      await context.service.delete(context.entityId);
    }
    
    return {
      success: true,
      shouldRefresh: true,
      shouldNavigate: '/list',
      message: 'Entity deleted successfully'
    };
  } catch (error) {
    return {
      success: false,
      error: error.message
    };
  }
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

