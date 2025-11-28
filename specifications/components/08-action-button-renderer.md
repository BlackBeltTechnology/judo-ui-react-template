# Action Button Renderer Specification

**Domain:** Components  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `components/07-action-executor-hook.md`, `runtime-model/06-button-model.md`  
**Blocks:** Button rendering implementation  

## Overview

Action Button Renderer renders buttons from ButtonModel, handling action execution, conditional rendering, and visual states.

## Button Component

```typescript
interface ActionButtonProps {
  button: ButtonModel;
  data: any;
  actions: Record<string, Function>;
  disabled?: boolean;
}

function ActionButton({ button, data, actions, disabled }: ActionButtonProps) {
  const { handleClick, isExecuting, isEnabled, isHidden } = useActionButton(
    actions[button.actionName],
    { entityData: data }
  );
  
  if (isHidden) return null;
  
  const buttonDisabled = disabled || !isEnabled || isExecuting;
  
  // Icon-only button
  if (button.buttonStyle === 'icon') {
    return (
      <Tooltip title={button.tooltipText || button.label}>
        <span>
          <IconButton
            onClick={handleClick}
            disabled={buttonDisabled}
            size="small"
          >
            {button.icon && <Icon name={button.icon.name} />}
            {isExecuting && <CircularProgress size={20} />}
          </IconButton>
        </span>
      </Tooltip>
    );
  }
  
  // Regular button
  return (
    <Button
      onClick={handleClick}
      disabled={buttonDisabled}
      variant={button.buttonStyle}
      startIcon={button.icon && <Icon name={button.icon.name} />}
      title={button.tooltipText}
    >
      {button.label}
      {isExecuting && <CircularProgress size={20} sx={{ ml: 1 }} />}
    </Button>
  );
}
```

## Button Group Renderer

```typescript
interface ButtonGroupRendererProps {
  group: ButtonGroupModel;
  data: any;
  actions: Record<string, Function>;
}

function ButtonGroupRenderer({ group, data, actions }: ButtonGroupRendererProps) {
  const featured = group.buttons.slice(0, group.featuredActions);
  const overflow = group.buttons.slice(group.featuredActions);
  
  return (
    <Box display="flex" gap={1} justifyContent="flex-end">
      {/* Featured buttons */}
      {featured.map(button => (
        <ActionButton
          key={button.id}
          button={button}
          data={data}
          actions={actions}
        />
      ))}
      
      {/* Overflow menu */}
      {overflow.length > 0 && (
        <OverflowMenu buttons={overflow} data={data} actions={actions} />
      )}
    </Box>
  );
}
```

## Overflow Menu

```typescript
function OverflowMenu({ buttons, data, actions }: Props) {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  
  const visibleButtons = buttons.filter(button => {
    const hidden = button.hiddenBy && data[button.hiddenBy];
    return !hidden;
  });
  
  if (visibleButtons.length === 0) return null;
  
  return (
    <>
      <IconButton onClick={(e) => setAnchorEl(e.currentTarget)}>
        <MoreVertIcon />
      </IconButton>
      
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={() => setAnchorEl(null)}
      >
        {visibleButtons.map(button => {
          const enabled = !button.enabledBy || data[button.enabledBy];
          
          return (
            <MenuItem
              key={button.id}
              onClick={async () => {
                await actions[button.actionName]();
                setAnchorEl(null);
              }}
              disabled={!enabled}
            >
              {button.icon && <ListItemIcon><Icon name={button.icon.name} /></ListItemIcon>}
              <ListItemText>{button.label}</ListItemText>
            </MenuItem>
          );
        })}
      </Menu>
    </>
  );
}
```

## Button Styles

```typescript
function getButtonVariant(style: ButtonStyle): 'text' | 'outlined' | 'contained' {
  const map = {
    text: 'text',
    outlined: 'outlined',
    contained: 'contained',
    icon: 'text',
    fab: 'contained'
  };
  return map[style] as any;
}

function getButtonComponent(style: ButtonStyle) {
  if (style === 'fab') {
    return Fab;
  }
  if (style === 'icon') {
    return IconButton;
  }
  return Button;
}
```

## Examples

### Example 1: Page Actions

```typescript
const pageActions: ButtonGroupModel = {
  id: 'page-actions',
  name: 'pageActions',
  featuredActions: 3,
  buttons: [
    { name: 'refresh', actionName: 'refresh', buttonStyle: 'text' },
    { name: 'create', actionName: 'create', buttonStyle: 'contained' },
    { name: 'export', actionName: 'export', buttonStyle: 'text' }
  ]
};

<ButtonGroupRenderer
  group={pageActions}
  data={data}
  actions={actions}
/>
```

### Example 2: Row Actions

```typescript
function TableRowActions({ row, actions }: Props) {
  const rowActionButtons: ButtonModel[] = [
    { name: 'view', actionName: 'view', buttonStyle: 'icon', icon: { name: 'visibility' } },
    { name: 'edit', actionName: 'edit', buttonStyle: 'icon', icon: { name: 'edit' } },
    { name: 'delete', actionName: 'delete', buttonStyle: 'icon', icon: { name: 'delete' } }
  ];
  
  return (
    <Box display="flex">
      {rowActionButtons.map(button => (
        <ActionButton key={button.name} button={button} data={row} actions={actions} />
      ))}
    </Box>
  );
}
```

### Example 3: Conditional Button

```typescript
const approveButton: ButtonModel = {
  name: 'approve',
  label: 'Approve',
  actionName: 'approve',
  buttonStyle: 'contained',
  enabledBy: 'canApprove',
  hiddenBy: 'isApproved'
};

<ActionButton button={approveButton} data={data} actions={actions} />
```

## Testing Criteria

### Unit Tests
- [x] Button renders correctly
- [x] Icon-only button works
- [x] Disabled state handled
- [x] Hidden button not rendered
- [x] Loading state shows

### Integration Tests
- [x] Button group renders
- [x] Overflow menu works
- [x] Actions execute on click
- [x] Conditional buttons work

---

**Status:** ✅ Complete  
**Created:** 2025-11-28

