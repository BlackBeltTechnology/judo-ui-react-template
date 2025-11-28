# Action Executor Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

```
}
  return { execute };
  
  );
    [executor]
    },
      return executor.execute(action, context);
    (action: ActionModel, context: ActionContext) => {
  const execute = useCallback(
  
  );
    [service, navigate, showToast]
    () => new ActionExecutor(service, navigate, showToast),
  const executor = useMemo(
  
  const service = useService();
  const { showToast } = useToast();
  const navigate = useNavigate();
function useActionExecutor() {
```typescript

## React Hook

```
}
  }
    }
      this.showToast(result.error, 'error');
    } else if (result.error) {
      }
        this.navigate(result.shouldNavigate);
      if (result.shouldNavigate) {
      
      }
        this.showToast(result.message, 'success');
      if (result.message) {
    if (result.success) {
  private async handleResult(result: ActionResult, action: ActionModel) {
  
  }
    return true;
    
    }
      return false;
    if (['update', 'delete'].includes(action.type) && !context.entityId) {
    // Check required context
    
    }
      return false;
    if (action.enabledBy && !context.entityData?.[action.enabledBy]) {
    // Check behavior conditions
  private canExecute(action: ActionModel, context: ActionContext): boolean {
  
  }
    }
        throw new Error(`Unknown action type: ${action.type}`);
      default:
        return this.executeCallOperation(action as CallOperationActionModel, context);
      case 'callOperation':
        return this.executeUnset(action as UnsetActionModel, context);
      case 'unset':
        return this.executeSet(action as SetActionModel, context);
      case 'set':
        return this.executeRemove(action as RemoveActionModel, context);
      case 'remove':
        return this.executeAdd(action as AddActionModel, context);
      case 'add':
        return this.executeDelete(action as DeleteActionModel, context);
      case 'delete':
        return this.executeUpdate(action as UpdateActionModel, context);
      case 'update':
        return this.executeCreate(action as CreateActionModel, context);
      case 'create':
        return this.executeRefresh(context);
      case 'refresh':
    switch (action.type) {
  ): Promise<ActionResult> {
    context: ActionContext
    action: ActionModel,
  private async performAction(
  
  }
    }
      return this.handleError(error, action);
    } catch (error) {
      
      return result;
      
      await this.handleResult(result, action);
      // 5. Handle result
      
      const result = await this.performAction(action, context);
      // 4. Execute
      
      }
        context = { ...context, preFetchData };
        );
          context
          this.findAction(action.preFetchActionName),
        const preFetchData = await this.execute(
      if (action.preFetchActionName) {
      // 3. Pre-fetch
      
      }
        }
          return { success: false };
        if (!confirmed) {
        const confirmed = await this.showConfirmation(action.confirmation, context);
      if (action.confirmation) {
      // 2. Confirmation
      
      }
        throw new Error('Action cannot be executed');
      if (!this.canExecute(action, context)) {
      // 1. Validate
    try {
  ): Promise<ActionResult> {
    context: ActionContext
    action: ActionModel,
  async execute(
  
  ) {}
    private showToast: (message: string, severity: string) => void
    private navigate: (path: string) => void,
    private service: any,
  constructor(
class ActionExecutor {
```typescript

## Core Implementation

Action Executor is the central engine that executes all actions with proper validation, confirmation, pre-fetch handling, and error management.

## Overview

**Blocks:** Action execution engine  
**Dependencies:** `runtime-model/05-action-model.md`, `actions/01-action-types.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Actions  


