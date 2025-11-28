# Confirmation System Specification

**Domain:** Actions  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/05-action-model.md`  
**Blocks:** Confirmation dialogs  

## Overview

Confirmation system provides user prompts before executing destructive or important actions.

## Types

```typescript
type ConfirmationType = 'conditional' | 'mandatory';

interface ConfirmationModel {
  type: ConfirmationType;
  title?: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  condition?: string;
}
```

## Implementation

```typescript
async function showConfirmation(
  confirmation: ConfirmationModel,
  context: ActionContext
): Promise<boolean> {
  // Evaluate condition for conditional confirmations
  if (confirmation.type === 'conditional' && confirmation.condition) {
    const shouldShow = evaluateCondition(confirmation.condition, context.entityData);
    if (!shouldShow) {
      return true;  // Skip confirmation
    }
  }
  
  // Show dialog
  return new Promise((resolve) => {
    const dialog = (
      <Dialog open={true}>
        <DialogTitle>{confirmation.title || 'Confirm Action'}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {interpolate(confirmation.message, context)}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => resolve(false)}>
            {confirmation.cancelLabel || 'Cancel'}
          </Button>
          <Button onClick={() => resolve(true)} color="primary" autoFocus>
            {confirmation.confirmLabel || 'Confirm'}
          </Button>
        </DialogActions>
      </Dialog>
    );
    
    showDialog(dialog);
  });
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

