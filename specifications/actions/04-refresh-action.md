# Refresh Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`, `actions/01-action-types.md`  
**Blocks:** Refresh implementations  

## Overview

Refresh action reloads current page data from the server without navigation.

## Action Model

```typescript
interface RefreshActionModel extends ActionModel {
  type: 'refresh';
  isBulk: false;
  isTransient: true;
}
```

## Implementation

```typescript
async function executeRefresh(context: ActionContext): Promise<ActionResult> {
  try {
    const data = await context.service.refresh(context.queryCustomizer);
    
    return {
      success: true,
      data,
      shouldRefresh: true,
      message: 'Data refreshed successfully'
    };
  } catch (error) {
    return {
      success: false,
      error: error.message
    };
  }
}
```

## Examples

### Example 1: Page Refresh

```typescript
const refreshAction: RefreshActionModel = {
  id: 'action-refresh',
  name: 'refresh',
  type: 'refresh',
  isBulk: false,
  isTransient: true
};

<Button onClick={() => executeAction(refreshAction, context)}>
  <RefreshIcon /> Refresh
</Button>
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

