# Button and ButtonGroup Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::Button`, `ui::ButtonGroup`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `02-visual-element.md`, `03-labeled-element.md`  
**Blocks:** Action execution, runtime models  

## Overview

`Button` represents clickable action triggers in the UI, while `ButtonGroup` organizes buttons into logical groupings. Buttons are the primary mechanism for executing actions (CRUD operations, navigation, custom operations) and can appear in page headers, table toolbars, row actions, and forms.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
├─ LabeledElement (abstract)
└─ Button

NamedElement (abstract)
└─ ButtonGroup
```

### Button Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Button identifier |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `label` | EString | 0..1 | - | Button text (inherited) |
| `icon` | Icon | 0..1 | - | Button icon (inherited) |
| `action` | Action | 1 | - | Action to execute |
| `preFetchAction` | Action | 0..1 | - | Action to execute before main action |
| `buttonStyle` | ButtonStyle | 0..1 | TEXT | Button visual style |
| `tooltipText` | EString | 0..1 | - | Tooltip on hover |
| `hiddenBy` | AttributeType | 0..1 | - | Conditional visibility |
| `enabledBy` | AttributeType | 0..1 | - | Conditional enablement |

### ButtonGroup Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Group identifier |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `buttons` | Button | 1..* | - | Buttons in this group |
| `featuredActions` | EInt | 0..1 | 3 | Number of buttons to show prominently |

### Enumerations

#### ButtonStyle
```
TEXT      = 0  // Text button (flat)
OUTLINED  = 1  // Outlined button
CONTAINED = 2  // Filled button (primary)
ICON      = 3  // Icon-only button
FAB       = 4  // Floating action button
```

### Relationships

**Button:**
- **References:** `action: Action` - The action to execute
- **References:** `preFetchAction: Action` - Optional pre-fetch action
- **References:** `hiddenBy: AttributeType` - Conditional visibility
- **References:** `enabledBy: AttributeType` - Conditional enablement

**ButtonGroup:**
- **Contains:** `buttons: Button[]` - Buttons in group
- **Referenced By:** `PageContainer.actionButtonGroups`

## Key Concepts

### Button Actions

Buttons trigger actions defined in the metamodel:
```xml
<button name="createUser" action="CreateUserAction">
  <icon name="add"/>
  <label>Create User</label>
</button>
```

Action execution happens through action system (see `actions/` specifications).

### Button States

**Enabled/Disabled:**
```xml
<button name="approve" 
        action="ApproveAction"
        enabledBy="Invoice#canApprove"/>
```

Runtime: `disabled = !data.canApprove`

**Visible/Hidden:**
```xml
<button name="delete" 
        action="DeleteAction"
        hiddenBy="User#isArchived"/>
```

Runtime: `hidden = data.isArchived === true`

### Pre-Fetch Actions

Some actions need data before executing:
```xml
<button name="edit" action="EditUserAction" preFetchAction="FetchFullUserAction"/>
```

Sequence:
1. Execute preFetchAction (e.g., load full entity)
2. Use result as input to main action
3. Execute main action (e.g., open edit form)

### Button Styles

**TEXT (Flat):** Secondary actions
```xml
<button name="cancel" buttonStyle="TEXT"/>
```

**OUTLINED:** Alternative primary actions
```xml
<button name="export" buttonStyle="OUTLINED"/>
```

**CONTAINED (Primary):** Main actions
```xml
<button name="save" buttonStyle="CONTAINED"/>
```

**ICON:** Space-constrained actions
```xml
<button name="refresh" buttonStyle="ICON">
  <icon name="refresh"/>
</button>
```

### Button Groups

Groups organize related buttons:
```xml
<actionButtonGroups id="mainActions" featuredActions="2">
  <buttons name="refresh" action="RefreshAction"/>
  <buttons name="create" action="CreateAction"/>
  <buttons name="export" action="ExportAction"/>
  <buttons name="import" action="ImportAction"/>
</actionButtonGroups>
```

**Featured Actions (2):** Refresh, Create shown prominently  
**Overflow Menu:** Export, Import in dropdown

## Examples from Sample Model

### Example 1: Simple Action Button

```xml
<button name="refresh" 
        action="RefreshAction"
        buttonStyle="TEXT">
  <icon name="refresh"/>
  <label>Refresh</label>
</button>
```

**Generated:**
```typescript
<Button
  startIcon={<RefreshIcon />}
  onClick={() => actions.refresh()}
  disabled={isLoading}
>
  {t('actions.refresh', { defaultValue: 'Refresh' })}
</Button>
```

### Example 2: Conditional Button

```xml
<button name="approve" 
        action="ApproveAction"
        buttonStyle="CONTAINED"
        enabledBy="Invoice#status"
        tooltipText="Approve this invoice">
  <icon name="check"/>
  <label>Approve</label>
</button>
```

**Generated:**
```typescript
<Tooltip title={t('tooltips.approve')}>
  <span>
    <Button
      variant="contained"
      startIcon={<CheckIcon />}
      onClick={() => actions.approve(data.id)}
      disabled={data.status !== 'PENDING' || isLoading}
    >
      {t('actions.approve')}
    </Button>
  </span>
</Tooltip>
```

### Example 3: Button with Pre-Fetch

```xml
<button name="edit" 
        action="EditUserAction"
        preFetchAction="GetUserForUpdateAction"
        buttonStyle="OUTLINED">
  <icon name="edit"/>
  <label>Edit</label>
</button>
```

**Generated:**
```typescript
<Button
  variant="outlined"
  startIcon={<EditIcon />}
  onClick={async () => {
    const fullData = await actions.getUserForUpdate(data.id);
    await actions.editUser(fullData);
  }}
>
  {t('actions.edit')}
</Button>
```

### Example 4: Button Group (Page Header)

```xml
<actionButtonGroups id="pageActions" featuredActions="2">
  <buttons name="refresh" action="RefreshAction" buttonStyle="TEXT">
    <icon name="refresh"/>
  </buttons>
  <buttons name="create" action="CreateAction" buttonStyle="CONTAINED">
    <icon name="add"/>
    <label>Create User</label>
  </buttons>
  <buttons name="export" action="ExportAction" buttonStyle="TEXT">
    <icon name="download"/>
    <label>Export</label>
  </buttons>
  <buttons name="import" action="ImportAction" buttonStyle="TEXT">
    <icon name="upload"/>
    <label>Import</label>
  </buttons>
</actionButtonGroups>
```

**Generated:**
```typescript
<Box display="flex" gap={1}>
  {/* Featured buttons */}
  <Button onClick={actions.refresh}>
    <RefreshIcon />
  </Button>
  <Button variant="contained" onClick={actions.create}>
    <AddIcon /> Create User
  </Button>
  
  {/* Overflow menu */}
  <Menu>
    <MenuItem onClick={actions.export}>
      <DownloadIcon /> Export
    </MenuItem>
    <MenuItem onClick={actions.import}>
      <UploadIcon /> Import
    </MenuItem>
  </Menu>
</Box>
```

### Example 5: Icon-Only Buttons

```xml
<button name="delete" 
        action="DeleteAction"
        buttonStyle="ICON"
        tooltipText="Delete user">
  <icon name="delete" color="error"/>
</button>
```

**Generated:**
```typescript
<Tooltip title="Delete user">
  <IconButton 
    color="error"
    onClick={() => actions.delete(data.id)}
  >
    <DeleteIcon />
  </IconButton>
</Tooltip>
```

### Example 6: Form Buttons

```xml
<actionButtonGroups id="formActions" featuredActions="2">
  <buttons name="save" action="SaveAction" buttonStyle="CONTAINED">
    <icon name="save"/>
    <label>Save</label>
  </buttons>
  <buttons name="cancel" action="CancelAction" buttonStyle="TEXT">
    <label>Cancel</label>
  </buttons>
</actionButtonGroups>
```

## Validation Rules

### Required Properties
- **Button:** `name`, `action`
- **ButtonGroup:** `name`, at least 1 button

### Constraints
- Button names unique within group
- Action must exist and be compatible
- EnabledBy/hiddenBy must reference boolean attributes
- Featured actions count ≤ total buttons
- Pre-fetch action must return compatible data

### Best Practices
- Primary action: CONTAINED style
- Secondary actions: TEXT or OUTLINED
- Icon-only for space-constrained areas
- Always provide tooltips for icon-only buttons
- Featured actions: most important 2-3 buttons

## Runtime Model Mapping

```typescript
interface ButtonModel {
  id: string;
  name: string;
  label?: string;
  icon?: IconModel;
  
  // Action
  actionName: string;              // action.name
  actionType: ActionType;          // action.type
  preFetchActionName?: string;     // preFetchAction.name
  
  // Style
  buttonStyle: 'text' | 'outlined' | 'contained' | 'icon' | 'fab';
  tooltipText?: string;
  
  // Conditional
  hiddenBy?: string;               // hiddenBy.name
  enabledBy?: string;              // enabledBy.name
  
  // Metadata
  sourceId?: string;
}

interface ButtonGroupModel {
  id: string;
  name: string;
  buttons: ButtonModel[];
  featuredActions: number;         // Number to show prominently
  overflowButtons?: ButtonModel[]; // Buttons in overflow menu
}
```

## Generator Implementation

See `generators/05-action-extractor.md`.

**Key Methods:**
```java
public class ButtonGenerator {
    public static ButtonModel extractButton(Button button);
    public static ButtonGroupModel extractButtonGroup(ButtonGroup group);
    public static List<Button> getFeaturedButtons(ButtonGroup group);
    public static List<Button> getOverflowButtons(ButtonGroup group);
    public static boolean isIconOnly(Button button);
}
```

## Common Patterns

### Button Rendering

```typescript
function ActionButton({ button, data, actions, isLoading }: Props) {
  const handleClick = async () => {
    if (button.preFetchActionName) {
      const preFetchResult = await actions[button.preFetchActionName](data.id);
      await actions[button.actionName](preFetchResult);
    } else {
      await actions[button.actionName](data.id);
    }
  };
  
  const disabled = button.enabledBy && !data[button.enabledBy] || isLoading;
  const hidden = button.hiddenBy && data[button.hiddenBy];
  
  if (hidden) return null;
  
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

### Button Group Rendering

```typescript
function ButtonGroupRenderer({ group, data, actions }: Props) {
  const featured = group.buttons.slice(0, group.featuredActions);
  const overflow = group.buttons.slice(group.featuredActions);
  
  return (
    <Box display="flex" gap={1}>
      {featured.map(button => (
        <ActionButton key={button.id} button={button} data={data} actions={actions} />
      ))}
      
      {overflow.length > 0 && (
        <OverflowMenu>
          {overflow.map(button => (
            <MenuItem key={button.id} onClick={() => executeAction(button)}>
              {button.icon && <Icon />}
              {button.label}
            </MenuItem>
          ))}
        </OverflowMenu>
      )}
    </Box>
  );
}
```

## Testing Criteria

### Unit Tests
- [x] Button extraction from metamodel
- [x] Button group extraction
- [x] Featured/overflow split
- [x] Action reference resolution
- [x] Conditional logic extraction

### Integration Tests
- [x] Buttons render correctly
- [x] Button clicks execute actions
- [x] Conditional buttons show/hide
- [x] Conditional buttons enable/disable
- [x] Button groups organize correctly
- [x] Overflow menu works

### Edge Cases
- [x] Button with no label (icon-only)
- [x] Button group with 0 featured actions
- [x] Button with missing action
- [x] Pre-fetch action failure
- [x] Very long button labels

## Related Specifications

**Metamodel:**
- `03-labeled-element.md` - Label and icon support
- `02-visual-element.md` - Base properties

**Runtime Model:**
- `runtime-model/06-button-model.md` - ButtonModel interface

**Actions:**
- All action specifications - Button actions

**Components:**
- `components/07-action-executor-hook.md` - Action execution

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

