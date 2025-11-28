# PageContainer Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::PageContainer`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `02-visual-element.md`, `05-page-definition.md`  
**Blocks:** Runtime models, container generation  

## Overview

`PageContainer` is the root container element within a PageDefinition that defines the page's content structure and type (Form, View, or Table). It specifies the data element, layout, and contains all visual elements and action button groups for the page.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
└─ VisualElement (abstract)
   └─ Container (abstract)
      └─ PageContainer
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Container name (typically "container") |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `type` | PageContainerType | 1 | - | FORM, VIEW, or TABLE |
| `dataElement` | DataElement | 0..1 | - | ClassType or RelationType this container operates on |
| `children` | VisualElement | 0..* | - | Visual elements within container |
| `actionButtonGroups` | ButtonGroup | 0..* | - | Groups of action buttons |

### Inherited Properties

From VisualElement:
- `col`, `row` - Grid positioning (typically not used for PageContainer)
- `size` - Fixed dimensions
- `stretch`, `fit` - Layout behavior
- Layout and conditional properties

### Relationships

**Contains:**
- `children: VisualElement[]` - All visual elements (inputs, tables, links, etc.)
- `actionButtonGroups: ButtonGroup[]` - Action button groups

**Referenced By:**
- `PageDefinition.container` - Page's main container

**References:**
- `dataElement: ClassType | RelationType` - Data this container binds to

### Enumerations

#### PageContainerType
```
FORM  = 0  // Single entity form with inputs
VIEW  = 1  // Single entity view (may include nested tables/links)
TABLE = 2  // Collection/list view with table
```

## Key Concepts

### Container Types

**FORM Container:**
- Edits single entity
- Contains input fields
- Draft mode for create/edit
- Typically in dialog

```xml
<container xsi:type="ui:PageContainer" 
           type="FORM"
           dataElement="User">
  <children xsi:type="ui:TextInput" name="firstName"/>
  <children xsi:type="ui:TextInput" name="lastName"/>
  <actionButtonGroups>
    <buttons actionDefinition="CreateAction"/>
    <buttons actionDefinition="CancelAction"/>
  </actionButtonGroups>
</container>
```

**VIEW Container:**
- Displays single entity
- Can be read-only or editable
- May contain tables, links, nested containers
- Full page layout

```xml
<container xsi:type="ui:PageContainer" 
           type="VIEW"
           dataElement="User">
  <children xsi:type="ui:TextInput" name="firstName"/>
  <children xsi:type="ui:Table" name="posts" dataElement="User#posts"/>
  <children xsi:type="ui:Link" name="profile"/>
  <actionButtonGroups>
    <buttons actionDefinition="RefreshAction"/>
    <buttons actionDefinition="UpdateAction"/>
  </actionButtonGroups>
</container>
```

**TABLE Container:**
- Lists multiple entities
- Contains single Table element
- Filtering, sorting, pagination
- Row actions

```xml
<container xsi:type="ui:PageContainer" 
           type="TABLE"
           dataElement="User">
  <children xsi:type="ui:Table" name="userTable">
    <!-- table configuration -->
  </children>
  <actionButtonGroups>
    <buttons actionDefinition="RefreshAction"/>
    <buttons actionDefinition="CreateAction"/>
  </actionButtonGroups>
</container>
```

### Data Element Binding

PageContainer binds to data:
- **ClassType:** For entity operations
- **RelationType:** For related entities
- **null:** For dashboard/custom pages

The dataElement determines:
- Available attributes for inputs
- Available relations for links/tables
- Service methods for CRUD operations

### Action Button Groups

Buttons are organized into groups:
```xml
<actionButtonGroups id="group1" featuredActions="2">
  <buttons action="refresh"/>
  <buttons action="create"/>
  <buttons action="export"/>
</actionButtonGroups>
```

Featured actions appear prominently; others in overflow menu.

## Examples from Sample Model

### Example 1: Form Container (Create)

```xml
<pages name="CreateUser" openInDialog="true">
  <container xsi:type="ui:PageContainer" 
             type="FORM"
             dataElement="User">
    <children xsi:type="ui:TextInput" name="firstName" col="6"/>
    <children xsi:type="ui:TextInput" name="lastName" col="6"/>
    <children xsi:type="ui:TextInput" name="email" col="12"/>
    <children xsi:type="ui:Checkbox" name="active" col="12"/>
    
    <actionButtonGroups>
      <buttons actionDefinition="CreateAction" label="Create"/>
      <buttons actionDefinition="CancelAction" label="Cancel"/>
    </actionButtonGroups>
  </container>
</pages>
```

### Example 2: View Container (Master-Detail)

```xml
<pages name="UserView">
  <container xsi:type="ui:PageContainer" 
             type="VIEW"
             dataElement="User">
    <!-- User details -->
    <children xsi:type="ui:Flex" direction="HORIZONTAL">
      <children xsi:type="ui:TextInput" name="firstName" col="6"/>
      <children xsi:type="ui:TextInput" name="lastName" col="6"/>
    </children>
    
    <!-- Related posts table -->
    <children xsi:type="ui:Table" 
              name="posts" 
              dataElement="User#posts"
              col="12"/>
    
    <!-- Related profile link -->
    <children xsi:type="ui:Link" 
              name="profile" 
              dataElement="User#profile"
              col="12"/>
    
    <actionButtonGroups>
      <buttons actionDefinition="RefreshAction"/>
      <buttons actionDefinition="UpdateAction"/>
      <buttons actionDefinition="DeleteAction"/>
    </actionButtonGroups>
  </container>
</pages>
```

### Example 3: Table Container (List)

```xml
<pages name="UserList">
  <container xsi:type="ui:PageContainer" 
             type="TABLE"
             dataElement="User">
    <children xsi:type="ui:Table" name="userTable">
      <columns name="firstName" attributeType="User#firstName"/>
      <columns name="lastName" attributeType="User#lastName"/>
      <columns name="email" attributeType="User#email"/>
      <columns name="active" attributeType="User#active"/>
      
      <rowActions action="viewUser"/>
      <rowActions action="editUser"/>
      <rowActions action="deleteUser"/>
    </children>
    
    <actionButtonGroups featuredActions="2">
      <buttons actionDefinition="RefreshAction"/>
      <buttons actionDefinition="CreateAction"/>
      <buttons actionDefinition="ExportAction"/>
    </actionButtonGroups>
  </container>
</pages>
```

### Example 4: Dashboard Container (No DataElement)

```xml
<pages name="Dashboard" dashboard="true">
  <container xsi:type="ui:PageContainer" type="VIEW">
    <!-- Custom dashboard widgets -->
    <children xsi:type="ui:Container" name="stats" col="12"/>
    <children xsi:type="ui:Container" name="recentActivity" col="6"/>
    <children xsi:type="ui:Container" name="quickActions" col="6"/>
  </container>
</pages>
```

## Validation Rules

### Required Properties
- `type` - Must be FORM, VIEW, or TABLE
- `name` - Typically "container"

### Constraints
- FORM containers should have input children
- TABLE containers must have exactly one Table child
- VIEW containers can have mixed children
- DataElement required except for dashboards
- ActionButtonGroups should have at least one button

### Type-Specific Rules
- **FORM:** Inputs for entity attributes
- **VIEW:** Can contain anything
- **TABLE:** Exactly one Table element

## Runtime Model Mapping

```typescript
interface PageContainerModel {
  id: string;
  name: string;
  type: 'form' | 'view' | 'table';
  dataElement?: string;              // FQN of ClassType/RelationType
  
  // Layout
  layout: LayoutModel;               // Flex layout configuration
  
  // Content
  visualElements: VisualElementModel[];
  actionButtonGroups: ButtonGroupModel[];
  
  // Validation
  validationRules?: ValidationRuleModel[];
}
```

## Generator Implementation

See `generators/03-container-model-generator.md`.

**Key Methods:**
```java
public class PageContainerGenerator {
    public static PageContainerModel extractContainerModel(PageContainer container);
    public static String getContainerType(PageContainer container);
    public static List<VisualElement> getChildren(PageContainer container);
    public static List<ButtonGroup> getActionButtonGroups(PageContainer container);
}
```

## Common Patterns

### Container Rendering

```typescript
function ModelDrivenContainer({ model, data, actions }: Props) {
  return (
    <Box>
      {/* Visual elements */}
      <Grid container spacing={2}>
        {model.visualElements.map(element => (
          <Grid item xs={element.col} key={element.id}>
            <VisualElementRegistry element={element} data={data} />
          </Grid>
        ))}
      </Grid>
      
      {/* Action buttons */}
      {model.actionButtonGroups.map(group => (
        <ButtonGroup key={group.id} group={group} actions={actions} />
      ))}
    </Box>
  );
}
```

## Testing Criteria

### Unit Tests
- [x] Container extraction from metamodel
- [x] Type determination
- [x] Children collection
- [x] Button group extraction
- [x] Data element resolution

### Integration Tests
- [x] Form container renders inputs
- [x] View container renders mixed content
- [x] Table container renders table
- [x] Action buttons functional
- [x] Data binding works

### Edge Cases
- [x] Empty container
- [x] Container with no actions
- [x] Dashboard container (no dataElement)
- [x] Deeply nested children

## Related Specifications

**Metamodel:**
- `02-visual-element.md` - Base class
- `05-page-definition.md` - Contains PageContainer
- `07-container.md` - Nested containers
- `13-table.md` - Table in TABLE containers

**Runtime Model:**
- `runtime-model/03-container-model.md` - TypeScript interface

**Components:**
- `components/02-model-driven-container.md` - React component

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

