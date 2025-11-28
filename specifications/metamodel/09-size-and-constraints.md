# Size and Constraints Specification
**Reviewed:** Not yet reviewed
**Last Updated:** 2025-11-28  
**Created:** 2025-11-28  
**Status:** ✅ Complete  

---

- `08-flex.md` - Layout interaction
- `02-visual-element.md` - Uses Size
**Metamodel:**

## Related Specifications

- [x] Negative values (should reject)
- [x] Zero dimensions
- [x] Very large sizes (>2000px)
- [x] Very small sizes (<10px)
### Edge Cases

- [x] Responsive behavior maintained
- [x] Elements don't overflow
- [x] Fixed sizes applied correctly
### Integration Tests

- [x] Both dimensions handling
- [x] Height-only handling
- [x] Width-only handling
- [x] Size extraction
### Unit Tests

## Testing Criteria

```
>
  }}
    height: size?.height
    width: { xs: '100%', md: size?.width },
  sx={{
<Box
```typescript

### Responsive Overrides

```
}
  };
    flexShrink: size.width || size.height ? 0 : undefined
    height: size.height ? `${size.height}px` : undefined,
    width: size.width ? `${size.width}px` : undefined,
  return {
  
  if (!size) return {};
function getSizeStyles(size?: SizeModel): CSSProperties {
```typescript

### CSS Generation

## Common Patterns

```
}
  // ...
  size?: SizeModel;
  // ...
interface VisualElementModel {
// Applied to element

}
  height?: number;  // Height in pixels
  width?: number;   // Width in pixels
interface SizeModel {
```typescript

## Runtime Model Mapping

- Avoid fixed sizing for form inputs (responsive issues)
  - Constrained containers
  - Specific UI elements (e.g., avatars)
  - Icons
  - Images
- Use fixed sizing for:
- Prefer flexible sizing when possible
### Best Practices

- Consider responsive design implications
- Very large values (>5000px) should warn
- At least one dimension should be specified
- Width and height must be positive integers
### Constraints

## Validation Rules

```
</children>
  <size width="100" height="100"/>
<children xsi:type="ui:Image" name="avatar">
```xml

### Example 4: Image with Fixed Dimensions

```
</children>
  <size width="250" height="600"/>
<children xsi:type="ui:Flex" name="sidebar" col="3">
```xml

### Example 3: Fixed Size Container

```
</children>
  <size height="200"/>
<children xsi:type="ui:TextArea" name="description" col="12">
```xml

### Example 2: Fixed Height TextArea

```
</children>
  <size width="120"/>
<children xsi:type="ui:TextInput" name="code" col="6">
```xml

### Example 1: Fixed Width Input

## Examples from Sample Model

Both dimensions fixed.
```
<size width="800" height="600"/>
```xml

### Both Dimensions

Fixed height, width adapts to container.
```
<size height="200"/>
```xml

### Height Only

Fixed width, height adapts to content.
```
<size width="400"/>
```xml

### Width Only

```
</children>
  <size width="300" height="40"/>
<children xsi:type="ui:TextInput" name="email" col="12">
```xml
**Fixed:** Elements have explicit dimensions

```
<children xsi:type="ui:TextInput" name="email" col="12"/>
```xml
**Flexible (Default):** Elements size based on content and container

### Fixed vs Flexible Sizing

## Key Concepts

```
}
  ...
  size: Size (0..1)
  ...
VisualElement {
```
Size is a property of VisualElement:

### Usage Context

```
}
  height: EInt  // Height in pixels
  width: EInt   // Width in pixels
Size {
```

### Composite Type

## Metamodel Definition

`Size` is a composite type used to specify fixed dimensions (width and height) for visual elements. It provides explicit sizing control when the default flexible layout is insufficient, enabling precise control over element dimensions.

## Overview

**Priority:** P1
**Blocks:** Layout specifications, responsive design  
**Dependencies:** `02-visual-element.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Ecore Class:** `ui::Size` (composite type)  
**Domain:** Metamodel  


