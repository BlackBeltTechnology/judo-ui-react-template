# VisualElement Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::VisualElement`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-named-element.md`  
**Blocks:** All visual element types, containers, pages  

## Overview

`VisualElement` is the abstract base class for all UI components that have a visual representation. It extends `NamedElement` and adds properties for layout, sizing, styling, and conditional rendering. All inputs, containers, tables, buttons, and display elements inherit from this class.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
└─ VisualElement (abstract)
   ├─ Input (abstract)
   │  ├─ TextInput
   │  ├─ NumericInput
   │  └─ ... (other inputs)
   ├─ Container
   │  ├─ Flex
   │  └─ TabController
   ├─ Table
   ├─ Button
   ├─ Link
   └─ ... (other visual elements)
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Unique identifier (inherited) |
| `sourceId` | EString | 0..1 | - | XMIID from source (inherited) |
| `annotations` | Annotation | 0..* | - | Custom annotations (inherited) |
| `col` | EInt | 0..1 | 12 | Grid column span (1-12) |
| `row` | EInt | 0..1 | - | Grid row position |
| `size` | Size | 0..1 | - | Fixed width/height constraints |
| `stretch` | Stretch | 0..1 | NONE | Stretch behavior in flex layouts |
| `fit` | Fit | 0..1 | LOOSE | How element fills available space |
| `hiddenBy` | AttributeType | 0..1 | - | Attribute controlling visibility |
| `enabledBy` | AttributeType | 0..1 | - | Attribute controlling enabled state |
| `requiredBy` | AttributeType | 0..1 | - | Attribute controlling required state |
| `subTheme` | EString | 0..1 | - | Custom theme variant |
| `autoFocus` | EBoolean | 0..1 | false | Whether element receives focus on mount |

### Operations

| Name | Return Type | Parameters | Description |
|------|-------------|------------|-------------|
| `getSeparator` | EString | - | Returns separator for FQN (inherited) |
| `getFQName` | EString | - | Returns fully qualified name (inherited) |
| `getOwner` | NamedElement | - | Returns owner element (inherited) |
| `isVisible` | EBoolean | data: Object | Evaluates visibility based on hiddenBy |
| `isEnabled` | EBoolean | data: Object | Evaluates enabled state based on enabledBy |
| `isRequired` | EBoolean | data: Object | Evaluates required state based on requiredBy |

### Relationships

**Contains:**
- Inherited from NamedElement

**Referenced By:**
- `Container.children` - Visual elements within containers
- `PageContainer.children` - Top-level page elements

**References:**
- `hiddenBy: AttributeType` - Attribute for conditional visibility
- `enabledBy: AttributeType` - Attribute for conditional enablement
- `requiredBy: AttributeType` - Attribute for conditional requirement

### Enumerations

#### Stretch
```
NONE       = 0  // No stretching (default)
HORIZONTAL = 1  // Stretch to fill width
VERTICAL   = 2  // Stretch to fill height
BOTH       = 3  // Stretch in both dimensions
```

#### Fit
```
NONE   = 0  // No fitting constraints
LOOSE  = 1  // Loosely fill space (default)
TIGHT  = 2  // Tightly fill space
```

#### Size (Composite)
```
width: EInt   // Fixed width in pixels
height: EInt  // Fixed height in pixels
```

## Key Concepts

### Grid Layout System

Based on 12-column grid:
- `col`: Specifies column span (1-12)
- `col=12`: Full width
- `col=6`: Half width
- `col=4`: Third width
- `col=3`: Quarter width

Multiple elements with `col` values summing to 12 create responsive rows.

### Conditional Rendering

Three conditional properties control element behavior:

1. **hiddenBy**: Controls visibility
   ```typescript
   // If hiddenBy references "isArchived" attribute
   hidden = data.isArchived === true
   ```

2. **enabledBy**: Controls enabled/disabled state
   ```typescript
   // If enabledBy references "isEditable" attribute
   disabled = data.isEditable === false
   ```

3. **requiredBy**: Controls required validation
   ```typescript
   // If requiredBy references "isActive" attribute
   required = data.isActive === true
   ```

### Stretch and Fit

**Stretch** determines how element expands:
- `NONE`: Natural size
- `HORIZONTAL`: Fill available width
- `VERTICAL`: Fill available height
- `BOTH`: Fill both dimensions

**Fit** determines space filling strategy:
- `NONE`: Use natural size
- `LOOSE`: Fill available space loosely
- `TIGHT`: Fill available space tightly

### Sizing

Fixed size constraints override responsive behavior:
```typescript
size: { width: 300, height: 200 }
```

## Examples from Sample Model

### Example 1: Simple Text Input (Half Width)

```xml
<children xsi:type="ui:TextInput" 
          name="firstName" 
          col="6"
          autoFocus="true"
          attributeType="User#firstName"/>
```

### Example 2: Conditional Visibility

```xml
<children xsi:type="ui:TextInput" 
          name="ssn" 
          col="12"
          hiddenBy="User#isForeign"
          attributeType="User#ssn"/>
```

**Runtime Behavior:**
```typescript
// Hidden when isForeign is true
const hidden = data.isForeign === true;
```

### Example 3: Conditional Enablement

```xml
<children xsi:type="ui:NumericInput" 
          name="salary" 
          col="6"
          enabledBy="User#isManager"
          attributeType="User#salary"/>
```

**Runtime Behavior:**
```typescript
// Disabled when isManager is false
const disabled = data.isManager === false;
```

### Example 4: Full Width with Stretch

```xml
<children xsi:type="ui:TextArea" 
          name="description" 
          col="12"
          stretch="BOTH"
          fit="TIGHT"
          attributeType="User#description"/>
```

### Example 5: Fixed Size Container

```xml
<children xsi:type="ui:Container" 
          name="sidebar" 
          col="3">
  <size width="250" height="500"/>
  <!-- child elements -->
</children>
```

### Example 6: Custom Theme

```xml
<children xsi:type="ui:Button" 
          name="deleteButton" 
          col="2"
          subTheme="danger"
          actionDefinition="DeleteAction"/>
```

## Validation Rules

### Required Properties
- `name` - Must be present (inherited)
- `col` - Defaults to 12 if not specified

### Constraints
- `col` must be between 1 and 12
- `row` must be non-negative if specified
- Size `width` and `height` must be positive if specified
- `hiddenBy`, `enabledBy`, `requiredBy` must reference valid AttributeType
- Conditional attributes must be boolean typed
- `subTheme` should match available theme variants

### Layout Rules
- Sum of `col` values in same row should equal 12 for optimal layout
- Elements with `stretch=BOTH` should not have fixed size
- `autoFocus` should only be true for one element per form

## Runtime Model Mapping

```typescript
interface VisualElementModel {
  id: string;                    // name or sourceId
  name: string;                  // name
  type: string;                  // Concrete type (e.g., 'textInput')
  
  // Layout
  col: number;                   // col (default 12)
  row?: number;                  // row
  
  // Sizing
  size?: {
    width?: number;              // size.width
    height?: number;             // size.height
  };
  stretch: 'none' | 'horizontal' | 'vertical' | 'both';  // stretch
  fit: 'none' | 'loose' | 'tight';                       // fit
  
  // Conditional rendering
  hiddenBy?: string;             // hiddenBy.name
  enabledBy?: string;            // enabledBy.name
  requiredBy?: string;           // requiredBy.name
  
  // Styling
  subTheme?: string;             // subTheme
  autoFocus?: boolean;           // autoFocus
  
  // Annotations
  annotations?: Record<string, string>;
}
```

## Generator Implementation

See `generators/01-generator-helpers.md` for Java implementation.

**Key Helper Methods:**
```java
public class VisualElementHelper {
    public static int getCol(VisualElement element);
    public static int getRow(VisualElement element);
    public static String getStretch(VisualElement element);
    public static String getFit(VisualElement element);
    public static String getConditionalAttribute(VisualElement element, String type);
    public static boolean hasConditionalRendering(VisualElement element);
    public static Map<String, Object> extractLayoutProps(VisualElement element);
}
```

## Common Patterns

### Responsive Layout

```typescript
// Half width on desktop, full width on mobile
<Element col={6} {...responsiveProps} />
```

### Conditional Rendering Logic

```typescript
// Runtime evaluation
const shouldRender = !element.hiddenBy || !data[element.hiddenBy];
const isDisabled = element.enabledBy && !data[element.enabledBy];
const isRequired = element.requiredBy && data[element.requiredBy];
```

### Grid System Usage

```typescript
// Two columns layout
<Element name="left" col={6} />
<Element name="right" col={6} />

// Three columns layout
<Element name="col1" col={4} />
<Element name="col2" col={4} />
<Element name="col3" col={4} />
```

## Testing Criteria

### Unit Tests
- [x] Col value clamped to 1-12 range
- [x] Default col value is 12
- [x] Stretch enum converted correctly
- [x] Fit enum converted correctly
- [x] Conditional attributes resolved
- [x] Size properties extracted

### Integration Tests
- [x] Grid layout renders correctly
- [x] Conditional visibility works
- [x] Conditional enablement works
- [x] Conditional required validation works
- [x] Stretch behavior applied
- [x] Fixed sizes respected
- [x] SubTheme styling applied

### Edge Cases
- [x] Col value exceeds 12 (should clamp or warn)
- [x] Negative row value (validation error)
- [x] Missing conditional attribute reference (graceful degradation)
- [x] Invalid stretch/fit value (fallback to default)
- [x] Size without width or height (partial sizing)
- [x] Multiple autoFocus elements (warning)

## Related Specifications

**Metamodel:**
- `01-named-element.md` - Base class
- `03-labeled-element.md` - Common subclass
- `04-reference-typed-visual-element.md` - Data-bound elements
- `11-input-base.md` - Input elements
- `06-page-container.md` - Container of visual elements
- `07-container.md` - Nested containers

**Runtime Model:**
- `runtime-model/01-core-types.md` - Base interfaces
- `runtime-model/04-visual-element-model.md` - TypeScript interface

**Visual Elements:**
- All `visual-elements/**/*.md` specifications

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

