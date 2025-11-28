# PageDefinition Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::PageDefinition`  
**Status:** Complete  
**Assigned:** Example  
**Dependencies:** None (uses base classes)  
**Blocks:** `runtime-model/02-page-model.md`, `generators/02-page-model-generator.md`

## Overview

`PageDefinition` is the top-level metamodel element that defines a complete page in the UI. It specifies the page's container (Form, View, or Table), available actions, navigation settings, and display properties.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
├─ LabeledElement (abstract)
└─ ReferenceTypedVisualElement (abstract)
   └─ PageDefinition
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Unique identifier for the page (inherited) |
| `label` | EString | 0..1 | - | Display label for the page (inherited) |
| `icon` | Icon | 0..1 | - | Icon for the page (inherited) |
| `dataElement` | DataElement | 0..1 | - | The data element this page operates on (inherited) |
| `container` | PageContainer | 1 | - | The main container defining page content |
| `actions` | Action | 0..* | - | Available actions for this page |
| `openInDialog` | EBoolean | 0..1 | false | Whether page opens in a dialog |
| `dialogSize` | DialogSize | 0..1 | MD | Size of dialog (XS, SM, MD, LG, XL) |
| `dashboard` | EBoolean | 1 | false | Whether this is a dashboard page |
| `generateActionsHook` | EBoolean | 1 | false | Generate customization hook for actions |
| `isSelector` | EBoolean | 0..1 | false | Whether this is a selector page |
| `isRelationSelector` | EBoolean | 0..1 | false | Whether this is a relation selector |
| `sourceId` | EString | 0..1 | - | XMIID from source model (inherited) |
| `annotations` | Annotation | 0..* | - | Custom annotations (inherited) |

### Operations

| Name | Return Type | Parameters | Description |
|------|-------------|------------|-------------|
| `getSeparator` | EString | - | Returns the separator character for FQN |
| `getFQName` | EString | - | Returns fully qualified name |
| `getOwner` | NamedElement | - | Returns the owner element |

### Relationships

**Contains:**
- `PageContainer` (1) - The page's content container
- `Action[]` (0..*) - Actions available on this page

**Referenced By:**
- `NavigationItem.target` - Navigation menu items
- `Action.targetPageDefinition` - Actions that navigate to this page
- `OpenPageActionDefinition` - Actions that open this page

**References:**
- `DataElement` (ClassType or RelationType) - The data this page operates on

### Enumerations

#### DialogSize
```
XS  = 0  // Extra small
SM  = 1  // Small
MD  = 2  // Medium (default)
LG  = 3  // Large
XL  = 4  // Extra large
```

## Key Concepts

### Page Types (Determined by Container)

1. **Form Page:** `container.type == FORM`
   - Creates/edits single entity
   - Draft mode enabled
   - Typically opened in dialog

2. **View Page:** `container.type == VIEW`
   - Displays single entity (read-only or editable)
   - Full page navigation
   - Can contain tables/links

3. **Table Page:** `container.type == TABLE`
   - Lists multiple entities
   - Filtering, sorting, pagination
   - Row actions

### Selector Pages

When `isSelector == true`:
- Page is used to select entities
- Returns selected items to caller
- Usually opened in dialog
- Special action handling

### Dashboard Pages

When `dashboard == true`:
- Empty container (no dataElement)
- Custom visualization
- Often uses custom components

## Examples from Sample Model

### Example 1: Simple Form Page

```xml
<pages name="CreateUser" 
       label="Create User"
       openInDialog="true"
       dialogSize="MD"
       isSelector="false">
  <container xsi:type="ui:PageContainer" 
             type="FORM"
             dataElement="User">
    <children xsi:type="ui:TextInput" 
              name="firstName" 
              label="First Name"
              attributeType="User#firstName"/>
    <!-- more fields -->
  </container>
  <actions name="create" 
           actionDefinition="CreateUserAction"/>
  <actions name="cancel" 
           actionDefinition="CancelAction"/>
</pages>
```

### Example 2: Table Page

```xml
<pages name="UserList" 
       label="Users"
       isSelector="false"
       dashboard="false">
  <container xsi:type="ui:PageContainer" 
             type="TABLE"
             dataElement="User">
    <children xsi:type="ui:Table" 
              name="userTable"
              dataElement="User">
      <columns name="firstName" 
               attributeType="User#firstName"/>
      <!-- more columns -->
    </children>
  </container>
  <actions name="refresh" 
           actionDefinition="RefreshAction"/>
  <actions name="create" 
           actionDefinition="CreateAction"/>
</pages>
```

### Example 3: Selector Page

```xml
<pages name="UserSelector" 
       label="Select User"
       openInDialog="true"
       dialogSize="LG"
       isSelector="true"
       isRelationSelector="true">
  <container xsi:type="ui:PageContainer" 
             type="TABLE"
             dataElement="User">
    <!-- table content -->
  </container>
  <actions name="range" 
           actionDefinition="SelectorRangeAction"/>
</pages>
```

### Example 4: Dashboard Page

```xml
<pages name="Dashboard" 
       label="Dashboard"
       dashboard="true">
  <container xsi:type="ui:PageContainer" 
             type="VIEW">
    <!-- Custom dashboard widgets -->
  </container>
</pages>
```

## Validation Rules

### Required Properties
- `name` - Must be unique within application
- `container` - Must be present

### Constraints
- If `openInDialog == true`, must have `dialogSize`
- If `isSelector == true`, container should typically be TABLE
- If `dashboard == true`, dataElement should be null
- If `isRelationSelector == true`, then `isSelector` must also be true

### Naming Conventions
- Name should be PascalCase
- Name should be descriptive of purpose
- Selectors should end with "Selector" (e.g., "UserSelector")
- Forms should describe action (e.g., "CreateUser", "EditUser")

## Runtime Model Mapping

See `runtime-model/02-page-model.md` for the corresponding TypeScript interface.

**Key Mappings:**
- `PageDefinition` → `PageModel`
- `container` → `container: ContainerModel`
- `actions` → `actions: ActionModel[]`
- `dialogSize` enum → `dialogSize: 'xs' | 'sm' | 'md' | 'lg' | 'xl'`
- `dataElement` reference → `dataElement: string` (FQN)

## Generator Implementation

See `generators/02-page-model-generator.md` for Java implementation.

**Key Methods:**
```java
public static PageModelData extractPageModel(PageDefinition page)
public static String getPageType(PageDefinition page)
public static boolean isFormPage(PageDefinition page)
public static boolean isViewPage(PageDefinition page)
public static boolean isTablePage(PageDefinition page)
```

## Component Implementation

See `components/01-model-driven-page.md` for React component.

**Component:**
```typescript
function ModelDrivenPage({ model, serviceImpl, signedIdentifier })
```

## Common Patterns

### Navigation to Page

```typescript
navigate(`/pages/${pageModel.name}/${signedIdentifier}`);
```

### Opening in Dialog

```typescript
const dialogResult = await openDialog(pageModel, { data });
```

### Selector Pattern

```typescript
const selected = await openSelector(selectorPageModel, {
  alreadySelected: currentSelection,
  allowSelectMultiple: true
});
```

## Testing Criteria

### Unit Tests
- [ ] Page model extraction from metamodel
- [ ] Property mapping completeness
- [ ] Validation rule enforcement
- [ ] FQN generation

### Integration Tests
- [ ] Form page renders correctly
- [ ] Table page with data loads
- [ ] Selector page returns selection
- [ ] Dashboard page with custom widgets
- [ ] Dialog opening and sizing
- [ ] Navigation between pages

### Edge Cases
- [ ] Empty container
- [ ] No actions defined
- [ ] Missing dataElement (dashboard)
- [ ] Invalid dialog size
- [ ] Circular page references

## Related Specifications

**Metamodel:**
- `06-page-container.md` - Container specifications
- `12-button-and-button-group.md` - Action buttons

**Runtime Model:**
- `02-page-model.md` - TypeScript PageModel interface
- `03-container-model.md` - TypeScript ContainerModel interface
- `05-action-model.md` - TypeScript ActionModel interface

**Actions:**
- `actions/05-navigation-actions.md` - Page navigation
- `actions/03-confirmation-system.md` - Dialog confirmations

**Components:**
- `components/01-model-driven-page.md` - Page component
- `components/02-model-driven-container.md` - Container component

**Generators:**
- `generators/02-page-model-generator.md` - Java generator
- `generators/08-page-component-generator.md` - Page component generator

## References

- **Ecore Definition:** `ui.ecore` lines 257-282
- **Original Proposal:** `RUNTIME_MODEL_GENERATION_PROPOSAL.md` section 2.1
- **Mapping Reference:** `METAMODEL_TO_RUNTIME_MAPPING.md` section "PageDefinition → PageModel"

