# OpenSelector Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Entity selector dialog  

## Overview

OpenSelector action opens a selector dialog for choosing one or more entities.

## Action Model

```typescript
interface OpenSelectorActionModel extends ActionModel {
  type: 'openSelector';
  targetPageName: string;
  targetType: string;
  relationName?: string;
  allowMultiple: boolean;
}
```

## Implementation

```typescript
async function executeOpenSelector(
  action: OpenSelectorActionModel,
  context: ActionContext
): Promise<ActionResult> {
  const selected = await openSelectorDialog({
    entityType: action.targetType,
    multiple: action.allowMultiple,
    exclude: context.exclude || []
  });
  
  if (selected) {
    return {
      success: true,
      data: selected
    };
  }
  
  return { success: false };
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

