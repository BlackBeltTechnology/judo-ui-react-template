# Remove Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Relation remove implementations  

## Overview

Remove action removes entities from MANY-cardinality relations without deleting them.

## Action Model

```typescript
interface RemoveActionModel extends ActionModel {
  type: 'remove';
  relationName: string;
  relationFqn: string;
  relationCardinality: 'many';
}
```

## Implementation

```typescript
async function executeRemove(action: RemoveActionModel, context: ActionContext): Promise<ActionResult> {
  const confirmed = await showConfirmation({
    type: 'optional',
    message: 'Remove this item from the list?'
  });
  
  if (!confirmed) return { success: false };
  
  try {
    await context.service.removeFromRelation(
      context.ownerData.id,
      action.relationName,
      context.entityId
    );
    
    return {
      success: true,
      shouldRefresh: true,
      message: 'Item removed'
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

