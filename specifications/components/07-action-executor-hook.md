# Action Executor Hook Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

- [x] Toast notifications show
- [x] Navigation works
- [x] Errors handled
- [x] Actions execute successfully
### Integration Tests

- [x] Confirmation works
- [x] Actions map created correctly
- [x] Tracks executing state
- [x] Hook executes actions
### Unit Tests

## Testing Criteria

```
}
  );
    </>
      )}
        />
          onCancel={handleCancel}
          onConfirm={handleConfirm}
          confirmation={action.confirmation}
        <ConfirmationDialog
      {confirmation && (
      
      </Button>
        {action.label}
      >
        disabled={!isEnabled || isExecuting}
        onClick={onClick}
      <Button 
    <>
  return (
  
  };
    await handleClick();
    }
      if (!confirmed) return;
      const confirmed = await requestConfirmation(action, context);
    if (action.confirmation) {
  const onClick = async () => {
  
  const { confirmation, requestConfirmation, handleConfirm, handleCancel } = useActionConfirmation();
  const { handleClick, isExecuting, isEnabled } = useActionButton(action, context);
function ActionButtonWithConfirmation({ action, context }: Props) {
```typescript

### Example 3: Action Button with Confirmation

```
}
  );
    />
      actions={actions}
      data={data}
      model={UserPageModel}
    <ModelDrivenPage
  return (
  
  });
    service: UserService
    entityData: data,
    entityId: userId,
  const actions = useActionsMap(UserPageModel.actions, {
  
  const [data, setData] = useState({});
function UserPage({ userId }: Props) {
```typescript

### Example 2: Actions Map for Page

```
}
  );
    </Button>
      Delete
    >
      disabled={executing[deleteAction.name]}
      onClick={handleDelete}
    <Button 
  return (
  
  };
    }
      navigate('/users');
    if (result.success) {
    
    });
      service: UserService
      entityId,
    const result = await execute(deleteAction, {
  const handleDelete = async () => {
  
  const { execute, executing } = useActionExecutor();
function DeleteButton({ entityId }: Props) {
```typescript

### Example 1: Execute Single Action

## Examples

```
}
  };
    handleCancel
    handleConfirm,
    requestConfirmation,
    confirmation,
  return {
  
  }, [confirmation]);
    setConfirmation(null);
    confirmation?.resolve(false);
  const handleCancel = useCallback(() => {
  
  }, [confirmation]);
    setConfirmation(null);
    confirmation?.resolve(true);
  const handleConfirm = useCallback(() => {
  
  }, []);
    });
      setConfirmation({ action, context, resolve });
    return new Promise((resolve) => {
  ): Promise<boolean> => {
    context: ActionContext
    action: ActionModel,
  const requestConfirmation = useCallback((
  
  } | null>(null);
    resolve: (confirmed: boolean) => void;
    context: ActionContext;
    action: ActionModel;
  const [confirmation, setConfirmation] = useState<{
function useActionConfirmation() {
```typescript

## Confirmation Hook

```
}
  };
    isHidden
    isEnabled,
    isExecuting,
    handleClick,
  return {
  
  const isHidden = action.hiddenBy && context.entityData?.[action.hiddenBy];
  const isEnabled = !action.enabledBy || context.entityData?.[action.enabledBy];
  const isExecuting = executing[action.name] || false;
  
  }, [action, context, execute]);
    await execute(action, context);
  const handleClick = useCallback(async () => {
  
  const { execute, executing } = useActionExecutor();
function useActionButton(action: ActionModel, context: ActionContext) {
```typescript

## Action Button Hook

```
}
  }, [actions, context, execute]);
    return map;
    
    });
      };
        return await execute(action, { ...context, args });
      map[action.name] = async (...args: any[]) => {
    actions.forEach(action => {
    
    const map: Record<string, Function> = {};
  return useMemo(() => {
  
  const { execute } = useActionExecutor();
) {
  context: Partial<ActionContext>
  actions: ActionModel[],
function useActionsMap(
```typescript

## Actions Map Hook

```
}
  return { execute, executing };
  
  }, [navigate, showToast]);
    }
      setExecuting(prev => ({ ...prev, [action.name]: false }));
    } finally {
      
      return result;
      
      }
        showToast(result.error, 'error');
      } else if (!result.success && result.error) {
        showToast(result.message, 'success');
      if (result.success && result.message) {
      
      const result = await executor.execute(action, context);
      const executor = new ActionExecutor(context.service, navigate, showToast);
    try {
    
    setExecuting(prev => ({ ...prev, [action.name]: true }));
  ): Promise<ActionResult> => {
    context: ActionContext
    action: ActionModel,
  const execute = useCallback(async (
  
  const [executing, setExecuting] = useState<Record<string, boolean>>({});
  const { t } = useTranslation();
  const { showToast } = useToast();
  const navigate = useNavigate();
function useActionExecutor() {
```typescript

## Core Hook

Action Executor Hook provides React integration for the action execution system, enabling components to execute actions with proper state management.

## Overview

**Blocks:** Action execution in components  
**Dependencies:** `actions/02-action-executor.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Components  


