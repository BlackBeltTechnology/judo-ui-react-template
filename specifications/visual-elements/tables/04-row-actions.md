# Row Actions Specification

**Domain:** Visual Elements / Tables  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `runtime-model/07-table-model.md`, `runtime-model/05-action-model.md`  

## Overview

Row actions define per-row operations in tables (view, edit, delete, custom operations).

## Row Actions Configuration

```typescript
interface RowActionConfig {
  actions: ActionModel[];
  iconActions: ActionModel[];      // Show as icon buttons
  menuActions: ActionModel[];      // Show in overflow menu
  maxIconActions: number;          // Default: 3
}
```

## Row Actions Component

```typescript
function RowActions({ row, actions, onAction }: RowActionsProps) {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  
  // Determine visible/hidden actions
  const iconActions = actions.slice(0, 3);
  const menuActions = actions.slice(3);
  
  return (
    <Box display="flex" justifyContent="flex-end">
      {iconActions.map(action => {
        const hidden = action.hiddenBy && row[action.hiddenBy];
        const disabled = action.enabledBy && !row[action.enabledBy];
        
        if (hidden) return null;
        
        return (
          <Tooltip key={action.id} title={action.name}>
            <IconButton
              size="small"
              onClick={() => onAction(action, row)}
              disabled={disabled}
            >
              <Icon name={getActionIcon(action.type)} />
            </IconButton>
          </Tooltip>
        );
      })}
      
      {menuActions.length > 0 && (
        <>
          <IconButton size="small" onClick={(e) => setAnchorEl(e.currentTarget)}>
            <MoreVertIcon />
          </IconButton>
          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
            {menuActions.map(action => (
              <MenuItem key={action.id} onClick={() => {
                onAction(action, row);
                setAnchorEl(null);
              }}>
                {action.name}
              </MenuItem>
            ))}
          </Menu>
        </>
      )}
    </Box>
  );
}
```

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

