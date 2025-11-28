# CallOperation Action Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Operation call implementations  

## Overview

CallOperation action executes custom business operations with optional parameters.

## Action Model

```typescript
interface CallOperationActionModel extends ActionModel {
  type: 'callOperation';
  operationName: string;
  operationKind: 'static' | 'instance';
  operationParameters: OperationParameterModel[];
}
```

## Implementation

```typescript
async function executeCallOperation(
  action: CallOperationActionModel,
  context: ActionContext
): Promise<ActionResult> {
  let params: Record<string, any> = {};
  
  // Collect parameters if needed
  if (action.operationParameters.length > 0) {
    params = await openParameterForm(action.operationParameters);
    if (!params) return { success: false };
  }
  
  try {
    let result;
    if (action.operationKind === 'instance') {
      result = await context.service[action.operationName](context.entityId, params);
    } else {
      result = await context.service[action.operationName](params);
    }
    
    return {
      success: true,
      data: result,
      shouldRefresh: true,
      message: 'Operation completed successfully'
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

### Example: Password Change

```typescript
const changePasswordAction: CallOperationActionModel = {
  id: 'action-change-password',
  name: 'changePassword',
  type: 'callOperation',
  operationName: 'changePassword',
  operationKind: 'instance',
  operationParameters: [
    { name: 'oldPassword', type: 'string', required: true },
    { name: 'newPassword', type: 'string', required: true }
  ]
};
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

