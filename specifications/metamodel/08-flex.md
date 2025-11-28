# Flex Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::Flex`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `07-container.md`  
**Blocks:** Layout patterns, responsive design  

## Overview

`Flex` is a concrete Container implementation that uses flexbox layout for arranging child elements. It provides powerful layout capabilities including direction, alignment, spacing, and wrapping control, enabling responsive and flexible UI layouts.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
└─ VisualElement (abstract)
   └─ Container (abstract)
      └─ Flex
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Flex container identifier |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `children` | VisualElement | 0..* | - | Child elements to layout |
| `direction` | FlexDirection | 0..1 | VERTICAL | Layout direction |
| `mainAxisAlignment` | MainAxisAlignment | 0..1 | START | Main axis alignment |
| `crossAxisAlignment` | CrossAxisAlignment | 0..1 | STRETCH | Cross axis alignment |
| `wrap` | FlexWrap | 0..1 | NO_WRAP | Wrapping behavior |
| `spacing` | EInt | 0..1 | 0 | Gap between children (in spacing units) |

### Inherited Properties

From Container and VisualElement:
- `col`, `row` - Grid positioning
- `size` - Fixed dimensions
- `stretch`, `fit` - Layout behavior
- `hiddenBy`, `enabledBy` - Conditional rendering

### Enumerations

#### FlexDirection
```
HORIZONTAL = 0  // Row layout (left to right)
VERTICAL   = 1  // Column layout (top to bottom)
```

#### MainAxisAlignment
```
START          = 0  // Align to start (left/top)
CENTER         = 1  // Center alignment
END            = 2  // Align to end (right/bottom)
SPACE_BETWEEN  = 3  // Space between items
SPACE_AROUND   = 4  // Space around items
SPACE_EVENLY   = 5  // Even spacing
```

#### CrossAxisAlignment
```
START   = 0  // Align to start
CENTER  = 1  // Center alignment
END     = 2  // Align to end
STRETCH = 3  // Stretch to fill
BASELINE = 4  // Align baselines
```

#### FlexWrap
```
NO_WRAP      = 0  // Single line, no wrapping
WRAP         = 1  // Wrap to multiple lines
WRAP_REVERSE = 2  // Wrap in reverse order
```

## Key Concepts

### Flexbox Layout Model

Flex uses CSS flexbox principles:
- **Main axis:** Direction of primary layout (horizontal or vertical)
- **Cross axis:** Perpendicular to main axis
- **Alignment:** Control positioning along both axes
- **Wrapping:** Handle overflow with multiple lines

### Direction

**HORIZONTAL (Row):**
```xml
<children xsi:type="ui:Flex" 
          name="row" 
          direction="HORIZONTAL">
  <children xsi:type="ui:TextInput" name="first" col="6"/>
  <children xsi:type="ui:TextInput" name="second" col="6"/>
</children>
```
Items arranged left to right.

**VERTICAL (Column):**
```xml
<children xsi:type="ui:Flex" 
          name="column" 
          direction="VERTICAL">
  <children xsi:type="ui:TextInput" name="first" col="12"/>
  <children xsi:type="ui:TextInput" name="second" col="12"/>
</children>
```
Items stacked top to bottom.

### Alignment

**Main Axis Alignment:**
- Controls spacing along primary direction
- START: Left (horizontal) or top (vertical)
- CENTER: Centered
- END: Right (horizontal) or bottom (vertical)
- SPACE_BETWEEN: Maximum space between items

**Cross Axis Alignment:**
- Controls positioning perpendicular to main axis
- STRETCH: Fill available space (default)
- START/CENTER/END: Specific positioning
- BASELINE: Align text baselines

### Spacing

Spacing creates gaps between children:
```xml
<children xsi:type="ui:Flex" 
          direction="HORIZONTAL"
          spacing="2">  <!-- 2 * spacing unit -->
```

Maps to theme spacing (e.g., `2 * 8px = 16px` gap).

## Examples from Sample Model

### Example 1: Horizontal Form Row

```xml
<children xsi:type="ui:Flex" 
          name="nameRow" 
          direction="HORIZONTAL"
          spacing="2"
          col="12">
  <children xsi:type="ui:TextInput" name="firstName" col="6"/>
  <children xsi:type="ui:TextInput" name="lastName" col="6"/>
</children>
```

**Renders as:** Two inputs side by side with gap.

### Example 2: Vertical Form Section

```xml
<children xsi:type="ui:Flex" 
          name="personalInfo" 
          direction="VERTICAL"
          spacing="3"
          col="12">
  <children xsi:type="ui:TextInput" name="firstName" col="12"/>
  <children xsi:type="ui:TextInput" name="lastName" col="12"/>
  <children xsi:type="ui:TextInput" name="email" col="12"/>
  <children xsi:type="ui:DateInput" name="birthDate" col="12"/>
</children>
```

**Renders as:** Stacked inputs with vertical spacing.

### Example 3: Centered Content

```xml
<children xsi:type="ui:Flex" 
          name="centeredActions" 
          direction="HORIZONTAL"
          mainAxisAlignment="CENTER"
          spacing="2"
          col="12">
  <children xsi:type="ui:Button" name="save"/>
  <children xsi:type="ui:Button" name="cancel"/>
</children>
```

**Renders as:** Buttons centered horizontally.

### Example 4: Space Between Layout

```xml
<children xsi:type="ui:Flex" 
          name="header" 
          direction="HORIZONTAL"
          mainAxisAlignment="SPACE_BETWEEN"
          crossAxisAlignment="CENTER"
          col="12">
  <children xsi:type="ui:Text" name="title"/>
  <children xsi:type="ui:Button" name="close"/>
</children>
```

**Renders as:** Title on left, close button on right.

### Example 5: Wrapped Grid

```xml
<children xsi:type="ui:Flex" 
          name="tags" 
          direction="HORIZONTAL"
          wrap="WRAP"
          spacing="1"
          col="12">
  <!-- Multiple tag chips that wrap to new lines -->
  <children xsi:type="ui:Chip" name="tag1"/>
  <children xsi:type="ui:Chip" name="tag2"/>
  <children xsi:type="ui:Chip" name="tag3"/>
  <!-- ... more tags -->
</children>
```

**Renders as:** Tags that wrap to multiple rows.

### Example 6: Complex Nested Layout

```xml
<children xsi:type="ui:Flex" 
          name="form" 
          direction="VERTICAL"
          spacing="3"
          col="12">
  
  <!-- Header section -->
  <children xsi:type="ui:Flex" 
            name="header" 
            direction="HORIZONTAL"
            mainAxisAlignment="SPACE_BETWEEN">
    <children xsi:type="ui:Text" name="title"/>
    <children xsi:type="ui:Button" name="help"/>
  </children>
  
  <!-- Fields section -->
  <children xsi:type="ui:Flex" 
            name="fields" 
            direction="VERTICAL"
            spacing="2">
    <children xsi:type="ui:Flex" 
              name="row1" 
              direction="HORIZONTAL"
              spacing="2">
      <children xsi:type="ui:TextInput" name="firstName" col="6"/>
      <children xsi:type="ui:TextInput" name="lastName" col="6"/>
    </children>
    <children xsi:type="ui:TextInput" name="email" col="12"/>
  </children>
  
  <!-- Actions section -->
  <children xsi:type="ui:Flex" 
            name="actions" 
            direction="HORIZONTAL"
            mainAxisAlignment="END"
            spacing="2">
    <children xsi:type="ui:Button" name="cancel"/>
    <children xsi:type="ui:Button" name="submit"/>
  </children>
</children>
```

## Validation Rules

### Required Properties
- `name` - Must be unique within parent

### Constraints
- Should have at least one child
- Direction must be HORIZONTAL or VERTICAL
- Alignment values must be valid enums
- Spacing should be non-negative

### Best Practices
- Use HORIZONTAL for form rows
- Use VERTICAL for form sections
- Use SPACE_BETWEEN for header/footer layouts
- Use wrapping for dynamic content (tags, chips)

## Runtime Model Mapping

```typescript
interface FlexModel extends ContainerModel {
  type: 'flex';
  direction: 'horizontal' | 'vertical';
  mainAxisAlignment: 'start' | 'center' | 'end' | 'space-between' | 'space-around' | 'space-evenly';
  crossAxisAlignment: 'start' | 'center' | 'end' | 'stretch' | 'baseline';
  wrap: 'nowrap' | 'wrap' | 'wrap-reverse';
  spacing: number;
  children: VisualElementModel[];
}

// Maps to CSS Flexbox
const cssMapping = {
  direction: {
    horizontal: 'row',
    vertical: 'column'
  },
  mainAxisAlignment: {
    start: 'flex-start',
    center: 'center',
    end: 'flex-end',
    'space-between': 'space-between',
    'space-around': 'space-around',
    'space-evenly': 'space-evenly'
  },
  crossAxisAlignment: {
    start: 'flex-start',
    center: 'center',
    end: 'flex-end',
    stretch: 'stretch',
    baseline: 'baseline'
  }
};
```

## Generator Implementation

See `generators/04-visual-element-extractor.md`.

**Key Methods:**
```java
public class FlexGenerator {
    public static FlexModel extractFlex(Flex flex);
    public static String getFlexDirection(Flex flex);
    public static String getMainAxisAlignment(Flex flex);
    public static String getCrossAxisAlignment(Flex flex);
    public static int getSpacing(Flex flex);
}
```

## Common Patterns

### Flex Rendering

```typescript
function FlexContainer({ model, data }: Props) {
  return (
    <Box
      display="flex"
      flexDirection={model.direction === 'horizontal' ? 'row' : 'column'}
      justifyContent={mapMainAxisAlignment(model.mainAxisAlignment)}
      alignItems={mapCrossAxisAlignment(model.crossAxisAlignment)}
      flexWrap={model.wrap}
      gap={model.spacing}
    >
      {model.children.map(child => (
        <VisualElementRenderer key={child.id} element={child} data={data} />
      ))}
    </Box>
  );
}
```

### Responsive Flex

```typescript
<Box
  display="flex"
  flexDirection={{ xs: 'column', md: 'row' }}  // Vertical on mobile, horizontal on desktop
  gap={2}
>
  {children}
</Box>
```

## Testing Criteria

### Unit Tests
- [x] Flex extraction from metamodel
- [x] Direction mapping
- [x] Alignment mapping
- [x] Spacing extraction
- [x] Wrap behavior

### Integration Tests
- [x] Horizontal layout renders correctly
- [x] Vertical layout renders correctly
- [x] Alignment applied correctly
- [x] Spacing creates gaps
- [x] Wrapping works
- [x] Nested flex containers work

### Edge Cases
- [x] Empty flex container
- [x] Single child
- [x] Very large spacing values
- [x] Complex nesting

## Related Specifications

**Metamodel:**
- `07-container.md` - Base class
- `02-visual-element.md` - Inherited properties

**Runtime Model:**
- `runtime-model/03-container-model.md` - FlexModel interface

**Visual Elements:**
- `visual-elements/containers/01-flex-container.md` - Detailed implementation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

