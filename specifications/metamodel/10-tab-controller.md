# TabController Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::TabController`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `07-container.md`  
**Blocks:** Tabbed layouts, multi-section forms  
**Priority:** P1

## Overview

`TabController` is a Container implementation that organizes child elements into tabbed sections. It provides a tabbed navigation interface where each tab contains a separate set of visual elements, enabling complex multi-section forms and views.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
└─ VisualElement (abstract)
   └─ Container (abstract)
      └─ TabController
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Tab controller identifier |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `tabs` | Tab | 1..* | - | Tab definitions |
| `defaultTab` | EInt | 0..1 | 0 | Initially selected tab index |
| `tabPosition` | TabPosition | 0..1 | TOP | Tab bar position |

### Tab Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Tab identifier |
| `label` | EString | 1 | - | Tab label text |
| `icon` | Icon | 0..1 | - | Tab icon |
| `children` | VisualElement | 0..* | - | Elements in this tab |
| `disabled` | EBoolean | 0..1 | false | Whether tab is disabled |
| `hiddenBy` | AttributeType | 0..1 | - | Conditional visibility |

### Enumerations

#### TabPosition
```
TOP    = 0  // Tabs at top (default)
BOTTOM = 1  // Tabs at bottom
LEFT   = 2  // Tabs on left side
RIGHT  = 3  // Tabs on right side
```

## Key Concepts

### Tab Organization

Tabs organize related content into sections:
```xml
<children xsi:type="ui:TabController" name="userTabs" col="12">
  <tabs name="personal" label="Personal Information">
    <children xsi:type="ui:TextInput" name="firstName"/>
    <children xsi:type="ui:TextInput" name="lastName"/>
  </tabs>
  
  <tabs name="contact" label="Contact Information">
    <children xsi:type="ui:TextInput" name="email"/>
    <children xsi:type="ui:TextInput" name="phone"/>
  </tabs>
  
  <tabs name="address" label="Address">
    <children xsi:type="ui:TextInput" name="street"/>
    <children xsi:type="ui:TextInput" name="city"/>
  </tabs>
</children>
```

### Tab State

- Only one tab visible at a time
- Tab content lazy-loaded or pre-rendered
- Tab state preserved during navigation
- Validation per tab or across all tabs

### Conditional Tabs

```xml
<tabs name="premium" 
      label="Premium Features"
      hiddenBy="User#isPremium">
```

Tab hidden based on data condition.

## Examples from Sample Model

### Example 1: Basic Tabbed Form

```xml
<children xsi:type="ui:TabController" 
          name="userForm" 
          defaultTab="0"
          col="12">
  
  <tabs name="basic" label="Basic Info">
    <icon name="person"/>
    <children xsi:type="ui:TextInput" name="firstName" col="6"/>
    <children xsi:type="ui:TextInput" name="lastName" col="6"/>
    <children xsi:type="ui:DateInput" name="birthDate" col="6"/>
  </tabs>
  
  <tabs name="contact" label="Contact">
    <icon name="email"/>
    <children xsi:type="ui:TextInput" name="email" col="12"/>
    <children xsi:type="ui:TextInput" name="phone" col="12"/>
  </tabs>
  
  <tabs name="address" label="Address">
    <icon name="home"/>
    <children xsi:type="ui:TextInput" name="street" col="12"/>
    <children xsi:type="ui:TextInput" name="city" col="6"/>
    <children xsi:type="ui:TextInput" name="zip" col="6"/>
  </tabs>
</children>
```

### Example 2: Tabs with Tables

```xml
<children xsi:type="ui:TabController" name="userDetails" col="12">
  
  <tabs name="info" label="Information">
    <children xsi:type="ui:TextInput" name="name" readOnly="true"/>
    <children xsi:type="ui:TextInput" name="email" readOnly="true"/>
  </tabs>
  
  <tabs name="posts" label="Posts">
    <children xsi:type="ui:Table" 
              name="postsTable" 
              dataElement="User#posts"/>
  </tabs>
  
  <tabs name="comments" label="Comments">
    <children xsi:type="ui:Table" 
              name="commentsTable" 
              dataElement="User#comments"/>
  </tabs>
</children>
```

### Example 3: Conditional Tab

```xml
<children xsi:type="ui:TabController" name="orderTabs" col="12">
  
  <tabs name="details" label="Details">
    <!-- Order details -->
  </tabs>
  
  <tabs name="items" label="Items">
    <!-- Order items -->
  </tabs>
  
  <tabs name="payments" 
        label="Payments"
        hiddenBy="Order#isPaid">
    <!-- Payment information - hidden when paid -->
  </tabs>
</children>
```

### Example 4: Vertical Tabs

```xml
<children xsi:type="ui:TabController" 
          name="settings" 
          tabPosition="LEFT"
          col="12">
  
  <tabs name="general" label="General"/>
  <tabs name="security" label="Security"/>
  <tabs name="notifications" label="Notifications"/>
  <tabs name="privacy" label="Privacy"/>
</children>
```

## Validation Rules

### Required Properties
- `name` - Must be unique
- `tabs` - At least 1 tab required

### Constraints
- Tab names must be unique within controller
- DefaultTab index must be valid
- At least one tab should not be hidden
- Tab content should not be empty

### Best Practices
- 3-7 tabs optimal
- Use icons for clarity
- Group related content
- Consider mobile layout
- Validate per-tab or collectively

## Runtime Model Mapping

```typescript
interface TabControllerModel extends ContainerModel {
  type: 'tabController';
  tabs: TabModel[];
  defaultTab: number;
  tabPosition: 'top' | 'bottom' | 'left' | 'right';
}

interface TabModel {
  id: string;
  name: string;
  label: string;
  icon?: IconModel;
  children: VisualElementModel[];
  disabled: boolean;
  hiddenBy?: string;
}
```

## Generator Implementation

```java
public class TabControllerGenerator {
    public static TabControllerModel extractTabController(TabController tc);
    public static List<TabModel> extractTabs(TabController tc);
}
```

## Common Patterns

### Tab Rendering

```typescript
function TabControllerComponent({ model, data }: Props) {
  const [activeTab, setActiveTab] = useState(model.defaultTab);
  
  const visibleTabs = model.tabs.filter(tab => 
    !tab.hiddenBy || !data[tab.hiddenBy]
  );
  
  return (
    <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)}>
      {visibleTabs.map((tab, index) => (
        <Tab 
          key={tab.id}
          label={tab.label}
          icon={tab.icon && <Icon name={tab.icon.name} />}
          disabled={tab.disabled}
        />
      ))}
      
      {visibleTabs.map((tab, index) => (
        <TabPanel key={tab.id} value={activeTab} index={index}>
          {tab.children.map(child => (
            <VisualElementRenderer element={child} data={data} />
          ))}
        </TabPanel>
      ))}
    </Tabs>
  );
}
```

## Testing Criteria

### Unit Tests
- [x] TabController extraction
- [x] Tab extraction
- [x] Default tab handling

### Integration Tests
- [x] Tabs render correctly
- [x] Tab switching works
- [x] Conditional tabs show/hide
- [x] Tab content displays

### Edge Cases
- [x] Single tab
- [x] All tabs hidden (should show error)
- [x] Invalid default tab index

## Related Specifications

**Metamodel:**
- `07-container.md` - Base class

**Visual Elements:**
- `visual-elements/containers/02-tab-controller.md` - Detailed spec

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

