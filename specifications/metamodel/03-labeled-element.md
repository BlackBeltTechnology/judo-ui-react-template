# LabeledElement Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::LabeledElement`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-named-element.md`  
**Blocks:** `04-reference-typed-visual-element.md`, `05-page-definition.md`, most visual elements  

## Overview

`LabeledElement` is an abstract class that extends `NamedElement` to add internationalization support through labels and icons. It provides the foundation for any UI element that needs to display text or icons to users, including pages, visual elements, buttons, and actions.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
└─ LabeledElement (abstract)
   ├─ ReferenceTypedVisualElement (abstract)
   │  └─ PageDefinition
   ├─ VisualElement (also extends NamedElement separately)
   ├─ ActionDefinition (various types)
   ├─ Button
   └─ ... (other labeled elements)
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Unique identifier (inherited) |
| `sourceId` | EString | 0..1 | - | XMIID from source (inherited) |
| `annotations` | Annotation | 0..* | - | Custom annotations (inherited) |
| `label` | EString | 0..1 | - | Display text for the element |
| `icon` | Icon | 0..1 | - | Icon for the element |

### Operations

All operations inherited from `NamedElement`:

| Name | Return Type | Parameters | Description |
|------|-------------|------------|-------------|
| `getSeparator` | EString | - | Returns separator for FQN (inherited) |
| `getFQName` | EString | - | Returns fully qualified name (inherited) |
| `getOwner` | NamedElement | - | Returns owner element (inherited) |

### Relationships

**Contains:**
- Inherited from NamedElement

**Referenced By:**
- All UI elements that display labels or icons

### Complex Types

#### Icon (Composite)
```
name: EString      // Icon name/identifier (e.g., 'person', 'edit', 'delete')
color: EString     // Optional color override (e.g., 'primary', 'error', '#FF0000')
```

## Key Concepts

### Label for Display

The `label` property provides human-readable text:
- Used for buttons, form fields, page titles
- Supports internationalization (i18n)
- Falls back to `name` if not provided
- Can contain placeholders for dynamic content

### Label Generation Strategy

When label is not explicitly set:
1. Use `label` if provided
2. Fall back to `name` converted to title case
3. Look up i18n key: `judo.{context}.{name}`

Example:
```typescript
// name: "firstName"
// label: not set
// Generated label: "First Name"
// I18n key: "judo.user.firstName"
```

### Icon Integration

Icons enhance visual recognition:
- Material Icons naming convention
- Optional color theming
- Positioned based on context (before/after label)
- Supports icon-only buttons

**Common Icon Names:**
- `add`, `edit`, `delete`, `save`, `cancel`
- `refresh`, `search`, `filter`, `sort`
- `person`, `email`, `phone`, `calendar`
- `visibility`, `visibility_off`, `lock`, `lock_open`

### Internationalization Pattern

Labels use i18n keys:
```typescript
// Element definition
name: "createUser"
label: "Create User"

// Generated i18n key
t('judo.actions.createUser', { defaultValue: 'Create User' })
```

## Examples from Sample Model

### Example 1: Labeled Button

```xml
<children xsi:type="ui:Button" 
          name="saveButton" 
          label="Save Changes">
  <icon name="save" color="primary"/>
</children>
```

**Runtime:**
```typescript
<Button startIcon={<SaveIcon />} color="primary">
  {t('judo.buttons.saveButton', { defaultValue: 'Save Changes' })}
</Button>
```

### Example 2: Labeled Input Field

```xml
<children xsi:type="ui:TextInput" 
          name="email" 
          label="Email Address"
          attributeType="User#email">
  <icon name="email"/>
</children>
```

**Runtime:**
```typescript
<TextField
  label={t('judo.fields.email', { defaultValue: 'Email Address' })}
  InputProps={{
    startAdornment: <EmailIcon />
  }}
/>
```

### Example 3: Page with Label and Icon

```xml
<pages name="UserManagement" 
       label="User Management"
       sourceId="_page_001">
  <icon name="people" color="primary"/>
  <!-- page content -->
</pages>
```

**Runtime:**
```typescript
// In navigation/breadcrumb
<Typography variant="h5">
  <PeopleIcon color="primary" />
  {t('judo.pages.UserManagement', { defaultValue: 'User Management' })}
</Typography>
```

### Example 4: Action with Icon Only

```xml
<actions name="refresh" 
         label="Refresh Data"
         actionDefinition="RefreshAction">
  <icon name="refresh"/>
</actions>
```

**Runtime (Icon Button):**
```typescript
<IconButton 
  aria-label={t('judo.actions.refresh', { defaultValue: 'Refresh Data' })}
  onClick={handleRefresh}>
  <RefreshIcon />
</IconButton>
```

### Example 5: Label with No Icon

```xml
<children xsi:type="ui:TextInput" 
          name="description" 
          label="Description"
          attributeType="User#description"/>
```

### Example 6: Default Label (No Label Property)

```xml
<children xsi:type="ui:TextInput" 
          name="firstName" 
          attributeType="User#firstName"/>
```

**Generated Label:** "First Name" (from name)

## Validation Rules

### Required Properties
- `name` - Must be present (inherited)

### Constraints
- `label` should be meaningful and concise
- `label` should follow sentence case or title case
- `icon.name` should match available icon set
- `icon.color` should be valid color value or theme key
- Labels should not exceed reasonable length (100 chars recommended)

### Internationalization Rules
- Each label should have corresponding i18n key
- Default values should be in English
- I18n keys should follow consistent pattern
- Missing translations should fall back gracefully

### Icon Guidelines
- Use semantic icon names (describes purpose)
- Maintain consistent icon usage across similar elements
- Provide aria-labels when icon-only
- Consider accessibility (icon + text preferred)

## Runtime Model Mapping

```typescript
interface LabeledElementModel {
  id: string;           // name or sourceId
  name: string;         // name
  label?: string;       // label
  icon?: {              // icon
    name: string;       // icon.name
    color?: string;     // icon.color
  };
  
  // I18n support
  i18nKey?: string;     // Generated: judo.{context}.{name}
  defaultLabel?: string; // label or generated from name
}
```

### Key Mappings
- `label` → `label?: string`
- `icon` → `icon?: { name: string; color?: string }`
- Generated i18n key → `i18nKey: string`

## Generator Implementation

See `generators/01-generator-helpers.md` for Java implementation.

**Key Helper Methods:**
```java
public class LabeledElementHelper {
    public static String getLabel(LabeledElement element);
    public static String getDefaultLabel(LabeledElement element); // From name
    public static String getI18nKey(LabeledElement element, String context);
    public static boolean hasIcon(LabeledElement element);
    public static String getIconName(LabeledElement element);
    public static String getIconColor(LabeledElement element);
    public static String generateTitleCase(String name); // Convert camelCase
}
```

## Common Patterns

### Label with Fallback

```typescript
const displayLabel = element.label || generateLabelFromName(element.name);
const translatedLabel = t(element.i18nKey, { defaultValue: displayLabel });
```

### Icon Rendering

```typescript
const IconComponent = iconMap[element.icon?.name];
const iconColor = element.icon?.color || 'inherit';

return (
  <>
    {IconComponent && <IconComponent color={iconColor} />}
    {translatedLabel}
  </>
);
```

### Accessibility

```typescript
// Icon with label
<Button startIcon={<Icon />}>
  {label}
</Button>

// Icon only
<IconButton aria-label={label}>
  <Icon />
</IconButton>
```

### I18n Key Generation

```typescript
function generateI18nKey(element: LabeledElement, context: string): string {
  return `judo.${context}.${element.name}`;
}

// Examples:
// generateI18nKey(page, 'pages')      => 'judo.pages.UserForm'
// generateI18nKey(button, 'buttons')  => 'judo.buttons.saveButton'
// generateI18nKey(field, 'fields')    => 'judo.fields.firstName'
```

## Testing Criteria

### Unit Tests
- [x] Label extraction from element
- [x] Default label generation from name
- [x] I18n key generation follows pattern
- [x] Icon name and color extraction
- [x] Fallback to name when label missing
- [x] Title case conversion works correctly

### Integration Tests
- [x] Labels display correctly in UI
- [x] Icons render with correct component
- [x] I18n translations applied
- [x] Fallback to default label works
- [x] Icon colors respect theme
- [x] Accessibility labels present

### Edge Cases
- [x] Empty label string (use name)
- [x] Very long label (truncation/wrapping)
- [x] Special characters in label (escaping)
- [x] Missing icon name (no icon rendered)
- [x] Invalid icon color (fallback to default)
- [x] Label with HTML/markdown (sanitization)

## Related Specifications

**Metamodel:**
- `01-named-element.md` - Base class
- `02-visual-element.md` - Also extends LabeledElement concepts
- `04-reference-typed-visual-element.md` - Extends LabeledElement
- `05-page-definition.md` - Uses LabeledElement
- `12-button-and-button-group.md` - Primary use case

**Runtime Model:**
- `runtime-model/01-core-types.md` - Base interfaces with label support
- `runtime-model/09-i18n-model.md` - Internationalization

**Generators:**
- `generators/01-generator-helpers.md` - Label and icon helpers

**Visual Elements:**
- All visual element specifications use labels

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

