# Add Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Relation add implementations  

## Overview

Add action adds entities to MANY-cardinality relations.

## Action Model

```typescript
interface AddActionModel extends ActionModel {
  type: 'add';
  relationName: string;
  relationFqn: string;
  targetType: string;
  relationCardinality: 'many';
}
```

## Implementation

```typescript
async function executeAdd(action: AddActionModel, context: ActionContext): Promise<ActionResult> {
  // Open selector to choose entities
  const selected = await openSelector({
    entityType: action.targetType,
    multiple: true,
    exclude: context.ownerData[action.relationName]?.map(e => e.id) || []
  });
  
  if (selected && selected.length > 0) {
    try {
      await Promise.all(
        selected.map(entity =>
          context.service.addToRelation(
            context.ownerData.id,
            action.relationName,
            entity.id
          )
        )
      );
      
      return {
        success: true,
        shouldRefresh: true,
        message: `Added ${selected.length} item(s)`
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

