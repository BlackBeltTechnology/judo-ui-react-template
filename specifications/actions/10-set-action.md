# Set Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Relation set implementations  

## Overview

Set action assigns an entity to ONE-cardinality relation.

## Action Model

```typescript
interface SetActionModel extends ActionModel {
  type: 'set';
  relationName: string;
  relationFqn: string;
  targetType: string;
  relationCardinality: 'one';
}
```

## Implementation

```typescript
async function executeSet(action: SetActionModel, context: ActionContext): Promise<ActionResult> {
  const selected = await openSelector({
    entityType: action.targetType,
    multiple: false
  });
  
  if (selected) {
    try {
      await context.service.setRelation(
        context.ownerData.id,
        action.relationName,
        selected.id
      );
      
      return {
        success: true,
        shouldRefresh: true,
        message: 'Relation updated'
      };
    } catch (error) {
      return {
        success: false,
        error: error.message
      };
    }
  }
  
  return { success: false };
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

