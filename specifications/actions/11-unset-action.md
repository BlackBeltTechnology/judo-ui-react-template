# Unset Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Relation unset implementations  

## Overview

Unset action clears ONE-cardinality relation (sets to null).

## Action Model

```typescript
interface UnsetActionModel extends ActionModel {
  type: 'unset';
  relationName: string;
  relationFqn: string;
  relationCardinality: 'one';
}
```

## Implementation

```typescript
async function executeUnset(
  action: UnsetActionModel,
  context: ActionContext
): Promise<ActionResult> {
  const confirmed = await showConfirmation({
    type: 'optional',
    message: `Clear ${action.relationName}?`
  });
  
  if (!confirmed) return { success: false };
  
  try {
    await context.service.unsetRelation(
      context.ownerData.id,
      action.relationName
    );
    
    return {
      success: true,
      shouldRefresh: true,
      message: 'Relation cleared'
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

