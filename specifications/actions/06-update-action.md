# Update Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Update implementations  

## Overview

Update action saves changes to existing entity.

## Action Model

```typescript
interface UpdateActionModel extends ActionModel {
  type: 'update';
  targetType: string;
  isBulk: false;
}
```

## Implementation

```typescript
async function executeUpdate(action: UpdateActionModel, context: ActionContext): Promise<ActionResult> {
  try {
    const updated = await context.service.update(context.entityId, context.formData);
    
    return {
      success: true,
      data: updated,
      shouldRefresh: true,
      message: 'Entity updated successfully'
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

