# Container Specification
**Reviewed:** Not yet reviewed
**Last Updated:** 2025-11-28  
**Created:** 2025-11-28  
**Status:** ✅ Complete  

---

- `components/02-model-driven-container.md` - React rendering
**Components:**

- `visual-elements/containers/01-flex-container.md` - Flex implementation
**Visual Elements:**

- `runtime-model/03-container-model.md` - TypeScript interface
**Runtime Model:**

- `10-tab-controller.md` - Concrete implementation
- `08-flex.md` - Concrete implementation
- `06-page-container.md` - Special container type
- `02-visual-element.md` - Base class
**Metamodel:**

## Related Specifications

- [x] Circular nesting (should prevent)
- [x] Single child container
- [x] Deeply nested containers (>5 levels)
- [x] Empty container
### Edge Cases

- [x] Themed containers styled
- [x] Conditional display works
- [x] Children positioned properly
- [x] Nested containers work
- [x] Containers render correctly
### Integration Tests

- [x] Layout property extraction
- [x] Nesting depth calculation
- [x] Children collection
- [x] Container extraction from metamodel
### Unit Tests

## Testing Criteria

```
</Grid>
  ))}
    </Grid>
      {renderElement(child)}
    <Grid item xs={child.col} key={child.id}>
  {container.children.map(child => (
<Grid container spacing={2}>
```typescript

### Nested Grid Layout

```
if (!shouldShowContainer) return null;

}, [container.hiddenBy, data]);
  return !data[container.hiddenBy];
  if (!container.hiddenBy) return true;
const shouldShowContainer = useMemo(() => {
```typescript

### Conditional Container Display

```
}
  );
    </Box>
      ))}
        />
          data={data}
          element={child} 
          key={child.id} 
        <VisualElementRenderer 
      {container.children.map(child => (
    <Box className={`container-${container.name}`}>
  return (
function renderContainer(container: ContainerModel, data: any) {
```typescript

### Recursive Rendering

## Common Patterns

```
}
    public static boolean isNested(Container container);
    public static int calculateDepth(Container container);
    public static List<VisualElementModel> extractChildren(Container container);
    public static ContainerModel extractContainer(Container container);
public class ContainerGenerator {
```java
**Key Methods:**

See `generators/04-visual-element-extractor.md`.

## Generator Implementation

```
}
  subTheme?: string;
  fit: FitType;
  stretch: StretchType;
  enabledBy?: string;
  hiddenBy?: string;
  row?: number;
  col: number;
  // Inherited from VisualElement
  
  children: VisualElementModel[];
  // Children
  
  alignment?: string;
  spacing?: number;
  direction?: 'horizontal' | 'vertical';
  // Layout (specific to container type)
  
  type: 'flex' | 'tabController' | 'container';
  name: string;
  id: string;
interface ContainerModel extends VisualElementModel {
```typescript

## Runtime Model Mapping

- Consider responsive behavior
- Group related fields logically
- Don't over-nest (prefer flat structures)
- Use semantic names describing content
### Best Practices

- Children col values should sum appropriately
- Should have at least one child element
- Nesting depth should be reasonable (< 5 levels)
- Can contain any VisualElement
### Constraints

- `name` - Must be unique within parent
### Required Properties

## Validation Rules

```
</children>
  </children>
    <!-- Related information -->
  <children xsi:type="ui:Flex" name="sidebar" col="4">
  <!-- Sidebar -->
  
  </children>
    <!-- Main form fields -->
  <children xsi:type="ui:Flex" name="mainContent" col="8">
  <!-- Main content -->
          col="12">
          direction="HORIZONTAL"
          name="responsiveLayout" 
<children xsi:type="ui:Flex" 
```xml

### Example 6: Responsive Container

```
</children>
  <children xsi:type="ui:NumericInput" name="total" col="4"/>
  <children xsi:type="ui:NumericInput" name="tax" col="4"/>
  <children xsi:type="ui:NumericInput" name="subtotal" col="4"/>
  <!-- Order totals -->
  
            col="12"/>
            dataElement="Order#items"
            name="items" 
  <children xsi:type="ui:Table" 
  <!-- Order items table -->
  
  <children xsi:type="ui:DateInput" name="orderDate" col="6"/>
  <children xsi:type="ui:TextInput" name="orderNumber" col="6"/>
  <!-- Order header info -->
          col="12">
          direction="VERTICAL"
          name="orderDetails" 
<children xsi:type="ui:Flex" 
```xml

### Example 5: Mixed Content Container

```
</children>
  <children xsi:type="ui:Checkbox" name="acknowledged"/>
  <children xsi:type="ui:Text" name="warningMessage"/>
          col="12">
          subTheme="warning"
          direction="VERTICAL"
          name="warningSection" 
<children xsi:type="ui:Flex" 
```xml

### Example 4: Themed Container

**Runtime:** Entire section hidden when `isBasicUser === true`

```
</children>
  <children xsi:type="ui:TextInput" name="secretKey"/>
  <children xsi:type="ui:TextInput" name="apiKey"/>
          col="12">
          hiddenBy="User#isBasicUser"
          direction="VERTICAL"
          name="advancedSettings" 
<children xsi:type="ui:Flex" 
```xml

### Example 3: Conditional Container

```
</children>
  </children>
    <children xsi:type="ui:TextInput" name="zipCode" col="6"/>
    <children xsi:type="ui:TextInput" name="city" col="6"/>
    <children xsi:type="ui:TextInput" name="street" col="12"/>
            direction="VERTICAL">
            name="address" 
  <children xsi:type="ui:Flex" 
  <!-- Address section -->
  
  </children>
    <children xsi:type="ui:TextInput" name="phone" col="6"/>
    <children xsi:type="ui:TextInput" name="email" col="6"/>
            direction="HORIZONTAL">
            name="contactInfo" 
  <children xsi:type="ui:Flex" 
  <!-- Contact information section -->
  
  </children>
    <children xsi:type="ui:TextInput" name="lastName" col="6"/>
    <children xsi:type="ui:TextInput" name="firstName" col="6"/>
            direction="HORIZONTAL">
            name="personalInfo" 
  <children xsi:type="ui:Flex" 
  <!-- Personal information section -->
  
          col="12">
          direction="VERTICAL"
          name="userDetails" 
<children xsi:type="ui:Flex" 
```xml

### Example 2: Nested Containers

```
</children>
  <children xsi:type="ui:TextInput" name="lastName" col="6"/>
  <children xsi:type="ui:TextInput" name="firstName" col="6"/>
          col="12">
          direction="HORIZONTAL"
          name="nameSection" 
<children xsi:type="ui:Flex" 
```xml

### Example 1: Simple Grouping Container

## Examples from Sample Model

- Manage responsive behavior
- Control alignment
- Set spacing between children
- Define direction (horizontal/vertical in Flex)
Containers control layout of children:

### Container as Layout Tool

- **Conditional display** - Show/hide groups
- **Responsive layout** - Nested grid structure
- **Visual grouping** - Sections, panels, cards
- **Semantic grouping** - Related fields together
Containers group related elements:

### Layout Organization

```
</container>
  </children>
    <children xsi:type="ui:TextInput" name="phone"/>
    <children xsi:type="ui:TextInput" name="email"/>
  <children xsi:type="ui:Container" name="contactInfo">
  </children>
    <children xsi:type="ui:TextInput" name="lastName"/>
    <children xsi:type="ui:TextInput" name="firstName"/>
  <children xsi:type="ui:Container" name="personalInfo">
<container name="userInfo">
```xml
Containers enable nested structure:

### Hierarchical Composition

## Key Concepts

- Buttons
- Links
- Tables
- Other containers (nested)
- Input elements
**Can Contain:**

- PageContainer
- Parent containers
**Referenced By:**

- `children: VisualElement[]` - Nested elements (inputs, containers, tables, etc.)
**Contains:**

### Relationships

- `subTheme` - Theme variant
- `hiddenBy`, `enabledBy` - Conditional rendering
- `stretch`, `fit` - Layout behavior
- `size` - Fixed dimensions
- `col`, `row` - Grid positioning
From VisualElement:

### Inherited Properties

| `children` | VisualElement | 0..* | - | Contained visual elements |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `name` | EString | 1 | - | Container identifier |
|------|------|--------------|---------|-------------|
| Name | Type | Multiplicity | Default | Description |

### Properties

```
      └─ PageContainer (extends Container concepts)
      ├─ TabController
      ├─ Flex
   └─ Container (abstract)
└─ VisualElement (abstract)
NamedElement (abstract)
```

### Class Hierarchy

## Metamodel Definition

`Container` is an abstract base class for grouping and organizing visual elements. It provides composition capabilities, allowing visual elements to be nested hierarchically. Concrete implementations include Flex and TabController.

## Overview

**Blocks:** `08-flex.md`, nested container patterns  
**Dependencies:** `02-visual-element.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Ecore Class:** `ui::Container`  
**Domain:** Metamodel  


