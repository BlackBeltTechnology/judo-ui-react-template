# OpenForm Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Form dialog navigation  

## Overview

OpenForm action opens a modal dialog form for creating or editing entities.

## Action Model

```typescript
interface OpenFormActionModel extends ActionModel {
  type: 'openForm';
  targetPageName: string;
  targetType?: string;
}
```

## Implementation

```typescript
async function executeOpenForm(
  action: OpenFormActionModel,
  context: ActionContext
): Promise<ActionResult> {
  const formData = await openFormDialog({
    pageName: action.targetPageName,
    entityId: context.entityId,
    initialData: context.preFetchData
  });
  
  if (formData) {
    return {
      success: true,
      data: formData,
      shouldRefresh: true
    };
  }
  
  return { success: false };
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

