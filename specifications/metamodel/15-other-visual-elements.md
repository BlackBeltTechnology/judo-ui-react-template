# Other Visual Elements Specification

**Domain:** Metamodel  
**Ecore Class:** Various  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `02-visual-element.md`, `03-labeled-element.md`  
**Blocks:** Component specifications  
**Priority:** P1

## Overview

This specification covers additional visual element types that don't fit into the major categories (inputs, containers, tables, links). These elements provide display-only content, images, dividers, and other UI elements.

## Visual Element Types

### 1. Text

Display static or dynamic text content.

**Properties:**
- `value` - Text content or expression
- `variant` - Typography variant (h1-h6, body1, body2, caption)
- `color` - Text color

**Example:**
```xml
<children xsi:type="ui:Text" 
          name="title" 
          value="User Management"
          variant="H4"
          col="12"/>
```

### 2. Label

Display a label with optional icon, typically for read-only fields.

**Properties:**
- `label` - Label text
- `value` - Display value
- `icon` - Optional icon

**Example:**
```xml
<children xsi:type="ui:Label" 
          name="status" 
          label="Status"
          value="{data.status}"
          col="6"/>
```

### 3. Divider

Visual separator between sections.

**Properties:**
- `orientation` - HORIZONTAL or VERTICAL
- `variant` - FULL_WIDTH, INSET, MIDDLE

**Example:**
```xml
<children xsi:type="ui:Divider" 
          name="sectionDivider" 
          orientation="HORIZONTAL"
          variant="FULL_WIDTH"
          col="12"/>
```

### 4. Spacer

Empty space for layout purposes.

**Properties:**
- `height` - Vertical spacing
- `width` - Horizontal spacing

**Example:**
```xml
<children xsi:type="ui:Spacer" 
          name="spacer" 
          height="32"
          col="12"/>
```

### 5. Image

Display images.

**Properties:**
- `src` - Image source (URL or attribute)
- `alt` - Alternative text
- `objectFit` - CONTAIN, COVER, FILL, NONE

**Example:**
```xml
<children xsi:type="ui:Image" 
          name="avatar" 
          src="User#avatarUrl"
          alt="User avatar"
          objectFit="COVER">
  <size width="100" height="100"/>
</children>
```

### 6. Icon

Display icon only.

**Properties:**
- `iconName` - Icon identifier
- `size` - SMALL, MEDIUM, LARGE
- `color` - Icon color

**Example:**
```xml
<children xsi:type="ui:Icon" 
          name="warningIcon" 
          iconName="warning"
          size="LARGE"
          color="warning"/>
```

### 7. Chip

Display chip/badge element.

**Properties:**
- `label` - Chip text
- `color` - Chip color
- `variant` - FILLED, OUTLINED
- `onDelete` - Optional delete action

**Example:**
```xml
<children xsi:type="ui:Chip" 
          name="statusChip" 
          label="{data.status}"
          color="primary"
          variant="FILLED"/>
```

### 8. Progress

Display progress indicator.

**Properties:**
- `variant` - CIRCULAR, LINEAR
- `value` - Progress value (0-100)
- `indeterminate` - Boolean

**Example:**
```xml
<children xsi:type="ui:Progress" 
          name="uploadProgress" 
          variant="LINEAR"
          value="{data.uploadProgress}"/>
```

### 9. Alert

Display alert/notification messages.

**Properties:**
- `severity` - SUCCESS, INFO, WARNING, ERROR
- `message` - Alert text
- `dismissible` - Can be closed

**Example:**
```xml
<children xsi:type="ui:Alert" 
          name="warningAlert" 
          severity="WARNING"
          message="Please review before saving"
          dismissible="false"
          col="12"/>
```

### 10. Card

Container with elevation and padding.

**Properties:**
- `elevation` - Shadow depth (0-24)
- `children` - Card content

**Example:**
```xml
<children xsi:type="ui:Card" 
          name="infoCard" 
          elevation="2"
          col="12">
  <children xsi:type="ui:Text" name="title" variant="H6"/>
  <children xsi:type="ui:Text" name="content"/>
</children>
```

## Common Properties

All visual elements inherit from VisualElement:
- `name` - Unique identifier
- `col`, `row` - Grid positioning
- `hiddenBy`, `enabledBy` - Conditional rendering
- `size` - Fixed dimensions
- `stretch`, `fit` - Layout behavior

## Examples from Sample Model

### Example 1: Section Header with Divider

```xml
<children xsi:type="ui:Text" 
          name="sectionTitle" 
          value="Personal Information"
          variant="H5"
          col="12"/>

<children xsi:type="ui:Divider" 
          name="divider" 
          orientation="HORIZONTAL"
          col="12"/>

<children xsi:type="ui:Spacer" height="16" col="12"/>
```

### Example 2: Status Display

```xml
<children xsi:type="ui:Flex" direction="HORIZONTAL" col="12">
  <children xsi:type="ui:Icon" 
            name="statusIcon" 
            iconName="check_circle"
            color="success"/>
  <children xsi:type="ui:Text" 
            name="statusText" 
            value="Active"/>
</children>
```

### Example 3: Information Card

```xml
<children xsi:type="ui:Card" elevation="1" col="4">
  <children xsi:type="ui:Flex" direction="VERTICAL" spacing="2">
    <children xsi:type="ui:Text" 
              name="cardTitle" 
              value="Total Users"
              variant="H6"/>
    <children xsi:type="ui:Text" 
              name="cardValue" 
              value="{data.userCount}"
              variant="H3"/>
    <children xsi:type="ui:Chip" 
              label="+12% this month"
              color="success"
              size="small"/>
  </children>
</children>
```

### Example 4: Warning Alert

```xml
<children xsi:type="ui:Alert" 
          name="validationAlert" 
          severity="ERROR"
          message="Please fix validation errors"
          hiddenBy="Form#isValid"
          col="12"/>
```

## Validation Rules

### Required Properties
- `name` - Must be unique within container

### Constraints
- Text elements need value or expression
- Images need src
- Icons need iconName
- Size constraints appropriate for type

### Best Practices
- Use semantic variants (h1-h6 hierarchy)
- Provide alt text for images
- Use alerts sparingly
- Consider accessibility

## Runtime Model Mapping

```typescript
// Base for display elements
interface DisplayElementModel extends VisualElementModel {
  type: 'text' | 'label' | 'divider' | 'spacer' | 'image' | 'icon' | 'chip' | 'progress' | 'alert' | 'card';
  // Type-specific properties
  [key: string]: any;
}

// Examples
interface TextModel extends DisplayElementModel {
  type: 'text';
  value: string;
  variant: 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6' | 'body1' | 'body2' | 'caption';
  color?: string;
}

interface ImageModel extends DisplayElementModel {
  type: 'image';
  src: string;
  alt: string;
  objectFit: 'contain' | 'cover' | 'fill' | 'none';
}

interface AlertModel extends DisplayElementModel {
  type: 'alert';
  severity: 'success' | 'info' | 'warning' | 'error';
  message: string;
  dismissible: boolean;
}
```

## Generator Implementation

See `generators/04-visual-element-extractor.md`.

**Key Methods:**
```java
public class DisplayElementGenerator {
    public static DisplayElementModel extractDisplayElement(VisualElement element);
    public static String getElementType(VisualElement element);
    public static Map<String, Object> extractProperties(VisualElement element);
}
```

## Common Patterns

### Dynamic Text Display

```typescript
<Typography variant={model.variant}>
  {evaluateExpression(model.value, data)}
</Typography>
```

### Conditional Alert

```typescript
{!data.isValid && (
  <Alert severity="error">
    {model.message}
  </Alert>
)}
```

### Image with Fallback

```typescript
<img 
  src={data[model.src] || '/default-avatar.png'}
  alt={model.alt}
  style={{ objectFit: model.objectFit }}
/>
```

## Testing Criteria

### Unit Tests
- [x] Element extraction
- [x] Property mapping
- [x] Type determination

### Integration Tests
- [x] Elements render correctly
- [x] Dynamic content displays
- [x] Conditional elements show/hide
- [x] Styling applied correctly

### Edge Cases
- [x] Missing image sources
- [x] Very long text content
- [x] Invalid icon names
- [x] Empty values

## Related Specifications

**Metamodel:**
- `02-visual-element.md` - Base class
- `03-labeled-element.md` - Label support

**Visual Elements:**
- `visual-elements/other/02-text-display.md` - Text elements
- `visual-elements/other/03-image.md` - Images
- `visual-elements/other/04-indicators.md` - Progress, alerts
- `visual-elements/other/05-decorative.md` - Dividers, spacers

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

