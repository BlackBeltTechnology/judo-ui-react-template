# OpenPage Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Page navigation  

## Overview

OpenPage action navigates to a full page view of an entity.

## Action Model

```typescript
interface OpenPageActionModel extends ActionModel {
  type: 'openPage';
  targetPageName: string;
  targetPagePath: string;
}
```

## Implementation

```typescript
async function executeOpenPage(
  action: OpenPageActionModel,
  context: ActionContext
): Promise<ActionResult> {
  const path = action.targetPagePath.replace(':id', context.entityId || '');
  context.navigate(path);
  
  return {
    success: true,
    shouldNavigate: path
  };
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

