# Confirmation Dialog Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

- [x] Context interpolation
- [x] Conditional confirmation
- [x] Promise resolves correctly
- [x] Hook integration
### Integration Tests

- [x] Message interpolation works
- [x] Cancel callback works
- [x] Confirm callback works
- [x] Dialog renders correctly
### Unit Tests

## Testing Criteria

```
});
  entityData: { hasUnsavedChanges: true }
const confirmed = await confirm(saveConfirmation, {
// Only shows if hasUnsavedChanges is true in context

};
  message: 'You have unsaved changes. Save before leaving?'
  condition: 'hasUnsavedChanges',
  type: 'conditional',
const saveConfirmation: ConfirmationModel = {
```typescript

### Example 3: Conditional Confirmation

```
});
  entityIds: selectedIds
const confirmed = await confirm(bulkDeleteConfirmation, {

};
  confirmLabel: 'Delete All'
  message: 'Delete {count} selected items?',
  type: 'mandatory',
const bulkDeleteConfirmation: ConfirmationModel = {
```typescript

### Example 2: Bulk Delete with Count

```
}
  );
    </>
      {confirmationDialog}
      <Button onClick={handleDelete}>Delete</Button>
    <>
  return (
  
  };
    }
      await performDelete();
    if (confirmed) {
    const confirmed = await confirm(deleteConfirmation);
  const handleDelete = async () => {
  
  const { confirm, confirmationDialog } = useConfirmation();
function DeleteButton() {

};
  cancelLabel: 'Cancel'
  confirmLabel: 'Delete',
  message: 'Are you sure you want to delete this item? This action cannot be undone.',
  title: 'Confirm Delete',
  type: 'mandatory',
const deleteConfirmation: ConfirmationModel = {
```typescript

### Example 1: Delete Confirmation

## Examples

```
}
  });
    return match;
    }
      return String(context.entityData[key]);
    if (context.entityData && key in context.entityData) {
    }
      return String(context.entityIds.length);
    if (key === 'count' && context.entityIds) {
  return message.replace(/\{(\w+)\}/g, (match, key) => {
  
  if (!context) return message;
function interpolate(message: string, context?: ActionContext): string {
```typescript

## Message Interpolation

```
}
  };
    ) : null
      />
        onCancel={handleCancel}
        onConfirm={handleConfirm}
        context={state.context}
        confirmation={state.confirmation}
        open={state.open}
      <ConfirmationDialog
    confirmationDialog: state.open && state.confirmation ? (
    confirm,
  return {
  
  }, [state]);
    setState({ open: false, confirmation: null, resolve: null });
    state.resolve?.(false);
  const handleCancel = useCallback(() => {
  
  }, [state]);
    setState({ open: false, confirmation: null, resolve: null });
    state.resolve?.(true);
  const handleConfirm = useCallback(() => {
  
  }, []);
    });
      setState({ open: true, confirmation, context, resolve });
    return new Promise((resolve) => {
  ): Promise<boolean> => {
    context?: ActionContext
    confirmation: ConfirmationModel,
  const confirm = useCallback((
  
  });
    resolve: null
    confirmation: null,
    open: false,
  }>({
    resolve: ((value: boolean) => void) | null;
    context?: ActionContext;
    confirmation: ConfirmationModel | null;
    open: boolean;
  const [state, setState] = useState<{
function useConfirmation() {
```typescript

## Confirmation Hook

```
}
  );
    </Dialog>
      </DialogActions>
        </Button>
          {confirmLabel}
        <Button onClick={onConfirm} color="primary" variant="contained" autoFocus>
        </Button>
          {cancelLabel}
        <Button onClick={onCancel} color="inherit">
      <DialogActions>
      </DialogContent>
        <DialogContentText>{message}</DialogContentText>
      <DialogContent>
      <DialogTitle>{title}</DialogTitle>
    <Dialog open={open} onClose={onCancel}>
  return (
  
  const cancelLabel = confirmation.cancelLabel || t('common.cancel', { defaultValue: 'Cancel' });
  const confirmLabel = confirmation.confirmLabel || t('common.confirm', { defaultValue: 'Confirm' });
  const title = confirmation.title || t('common.confirm', { defaultValue: 'Confirm Action' });
  const message = interpolate(confirmation.message, context);
  
  const { t } = useTranslation();
}: ConfirmationDialogProps) {
  onCancel 
  onConfirm, 
  context,
  confirmation, 
  open, 
function ConfirmationDialog({ 

}
  onCancel: () => void;
  onConfirm: () => void;
  context?: ActionContext;
  confirmation: ConfirmationModel;
  open: boolean;
interface ConfirmationDialogProps {
```typescript

## Component Interface

Confirmation Dialog displays user confirmation prompts before executing destructive or important actions.

## Overview

**Blocks:** Action confirmations  
**Dependencies:** `actions/03-confirmation-system.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Components  


