# ButtonModel Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-core-types.md`, `05-action-model.md`  
**Blocks:** Button rendering, action execution  

## Overview

`ButtonModel` represents clickable UI buttons that trigger actions. Buttons can appear in page headers, button groups, table toolbars, or as standalone elements.

## TypeScript Interfaces

### ButtonModel

```typescript
interface ButtonModel extends BaseModel {
  // Identification
  id: string;
  name: string;
  
  // Display
  label?: string;
  icon?: IconModel;
  tooltipText?: string;
  
  // Action
  actionName: string;                   // Reference to ActionModel
  actionType: ActionType;               // Type of action being triggered
  preFetchActionName?: string;          // Optional pre-fetch action
  
  // Style
  buttonStyle: ButtonStyle;             // 'text' | 'outlined' | 'contained' | 'icon' | 'fab'
  
  // Conditional
  hiddenBy?: string;
  enabledBy?: string;
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}
```

### ButtonGroupModel

```typescript
interface ButtonGroupModel extends BaseModel {
  // Identification
  id: string;
  name: string;
  
  // Buttons
  buttons: ButtonModel[];
  featuredActions: number;              // Number of buttons to show prominently
  
  // Computed
  overflowButtons?: ButtonModel[];      // Buttons in overflow menu
  
  // Metadata
  sourceId?: string;
}
```

## Examples

### Example 1: Primary Action Button

```typescript
const saveButton: ButtonModel = {
  id: 'btn-save',
  name: 'save',
  label: 'Save',
  icon: { name: 'save', color: 'inherit' },
  tooltipText: 'Save changes',
  actionName: 'update',
  actionType: 'update',
  buttonStyle: 'contained'
};
```

### Example 2: Secondary Action Button

```typescript
const cancelButton: ButtonModel = {
  id: 'btn-cancel',
  name: 'cancel',
  label: 'Cancel',
  actionName: 'cancel',
  actionType: 'close',
  buttonStyle: 'text'
};
```

### Example 3: Icon-Only Button

```typescript
const refreshButton: ButtonModel = {
  id: 'btn-refresh',
  name: 'refresh',
  icon: { name: 'refresh' },
  tooltipText: 'Refresh data',
  actionName: 'refresh',
  actionType: 'refresh',
  buttonStyle: 'icon'
};
```

### Example 4: Conditional Button

```typescript
const approveButton: ButtonModel = {
  id: 'btn-approve',
  name: 'approve',
  label: 'Approve',
  icon: { name: 'check' },
  actionName: 'approve',
  actionType: 'callOperation',
  buttonStyle: 'contained',
  enabledBy: 'canApprove',              // Only enabled when canApprove is true
  hiddenBy: 'isApproved'                // Hidden when already approved
};
```

### Example 5: Button with Pre-Fetch

```typescript
const editButton: ButtonModel = {
  id: 'btn-edit',
  name: 'edit',
  label: 'Edit',
  icon: { name: 'edit' },
  actionName: 'edit',
  actionType: 'openForm',
  preFetchActionName: 'getUserForUpdate',
  buttonStyle: 'outlined'
};
```

### Example 6: Delete Button with Confirmation

```typescript
const deleteButton: ButtonModel = {
  id: 'btn-delete',
  name: 'delete',
  label: 'Delete',
  icon: { name: 'delete', color: 'error' },
  tooltipText: 'Delete user',
  actionName: 'delete',
  actionType: 'delete',
  buttonStyle: 'text'
  // Confirmation handled in ActionModel
};
```

### Example 7: Button Group (Page Actions)

```typescript
const pageActionGroup: ButtonGroupModel = {
  id: 'group-page-actions',
  name: 'pageActions',
  featuredActions: 2,
  buttons: [
    {
      id: 'btn-refresh',
      name: 'refresh',
      icon: { name: 'refresh' },
      actionName: 'refresh',
      actionType: 'refresh',
      buttonStyle: 'text'
    },
    {
      id: 'btn-create',
      name: 'create',
      label: 'Create User',
      icon: { name: 'add' },
      actionName: 'create',
      actionType: 'create',
      buttonStyle: 'contained'
    },
    {
      id: 'btn-export',
      name: 'export',
      label: 'Export',
      icon: { name: 'download' },
      actionName: 'export',
      actionType: 'export',
      buttonStyle: 'text'
    },
    {
      id: 'btn-import',
      name: 'import',
      label: 'Import',
      icon: { name: 'upload' },
      actionName: 'import',
      actionType: 'import',
      buttonStyle: 'text'
    }
  ]
};

// Featured buttons: refresh, create
// Overflow buttons: export, import
```

### Example 8: Row Actions

```typescript
const rowActions: ButtonModel[] = [
  {
    id: 'btn-row-view',
    name: 'view',
    icon: { name: 'visibility' },
    tooltipText: 'View details',
    actionName: 'view',
    actionType: 'openPage',
    buttonStyle: 'icon'
  },
  {
    id: 'btn-row-edit',
    name: 'edit',
    icon: { name: 'edit' },
    tooltipText: 'Edit',
    actionName: 'edit',
    actionType: 'openForm',
    buttonStyle: 'icon',
    enabledBy: 'isEditable'
  },
  {
    id: 'btn-row-delete',
    name: 'delete',
    icon: { name: 'delete', color: 'error' },
    tooltipText: 'Delete',
    actionName: 'delete',
    actionType: 'delete',
    buttonStyle: 'icon',
    enabledBy: 'isDeletable'
  }
];
```

## Button Rendering

```typescript
function ActionButton({ button, data, actions, isLoading }: Props) {
  // Resolve action
  const action = actions[button.actionName];
  
  // Check conditional rendering
  const hidden = button.hiddenBy && data[button.hiddenBy];
  const disabled = 
    (button.enabledBy && !data[button.enabledBy]) || 
    isLoading;
  
  if (hidden) return null;
  
  // Handle click
  const handleClick = async () => {
    if (button.preFetchActionName) {
      const preFetchData = await actions[button.preFetchActionName]();
      await actions[button.actionName](preFetchData);
    } else {
      await actions[button.actionName]();
    }
  };
  
  // Render based on style
  if (button.buttonStyle === 'icon') {
    return (
      <Tooltip title={button.tooltipText || button.label}>
        <span>
          <IconButton onClick={handleClick} disabled={disabled}>
            <Icon name={button.icon?.name} color={button.icon?.color} />
          </IconButton>
        </span>
      </Tooltip>
    );
  }
  
  return (
    <Button
      variant={button.buttonStyle}
      startIcon={button.icon && <Icon name={button.icon.name} />}
      onClick={handleClick}
      disabled={disabled}
      title={button.tooltipText}
    >
      {t(`actions.${button.name}`, { defaultValue: button.label })}
    </Button>
  );
}
```

## Button Group Rendering

```typescript
function ButtonGroupRenderer({ group, data, actions }: Props) {
  const featured = group.buttons.slice(0, group.featuredActions);
  const overflow = group.buttons.slice(group.featuredActions);
  
  return (
    <Box display="flex" gap={1}>
      {/* Featured buttons */}
      {featured.map(button => (
        <ActionButton key={button.id} button={button} data={data} actions={actions} />
      ))}
      
      {/* Overflow menu */}
      {overflow.length > 0 && (
        <OverflowMenu>
          {overflow.map(button => (
            <MenuItem 
              key={button.id}
              onClick={() => executeAction(button, data)}
              disabled={button.enabledBy && !data[button.enabledBy]}
            >
              {button.icon && <Icon name={button.icon.name} />}
              {button.label}
            </MenuItem>
          ))}
        </OverflowMenu>
      )}
    </Box>
  );
}
```

## Validation Rules

- Button `name` must be unique within group
- `actionName` must reference valid action
- `buttonStyle` must be valid style
- Icon-only buttons should have `tooltipText`
- Featured actions count must be <= total buttons
- At least one featured action recommended

## Related Specifications

**Metamodel:**
- `metamodel/12-button-and-button-group.md` - Source specification

**Runtime Model:**
- `01-core-types.md` - Base types
- `05-action-model.md` - Actions triggered by buttons

**Components:**
- `components/08-action-button-renderer.md` - Button rendering

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

